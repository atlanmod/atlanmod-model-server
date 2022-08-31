package org.atlantmod.modelserver;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emfcloud.modelserver.emf.common.DefaultModelResourceManager;
import org.eclipse.emfcloud.modelserver.emf.common.watchers.ModelWatchersManager;
import org.eclipse.emfcloud.modelserver.emf.configuration.EPackageConfiguration;
import org.eclipse.emfcloud.modelserver.emf.configuration.ServerConfiguration;
import org.eclipse.emfcloud.modelserver.emf.util.JsonPatchHelper;

import java.io.File;
import java.io.FileFilter;
import java.util.Optional;
import java.util.Set;

/**
 * A resource manager that automatically registers loaded metamodels.
 * <p>
 * This manager has a dedicated loading strategy that takes into account potential metamodels in the provided source
 * directory. It first iterates the contents of the directory to find metamodel files, load the corresponding
 * resources, and registers the associated {@link EPackage}s. Then it looks for models in the source directory and
 * loads them. <b>This strategy allows to load models that conform to metamodels also loaded in the workspace</b>.
 *
 * @see #loadSourceResources(String)
 */
public class MetamodelAwareModelResourceManager extends DefaultModelResourceManager {

    /**
     * The {@link Logger} for this class.
     */
    protected static final Logger log = LogManager.getLogger(MetamodelAwareModelResourceManager.class);

    @Inject
    public MetamodelAwareModelResourceManager(final Set<EPackageConfiguration> configurations,
                                              final AdapterFactory adapterFactory,
                                              final ServerConfiguration serverConfiguration,
                                              final ModelWatchersManager watchersManager,
                                              final Provider<JsonPatchHelper> jsonPatchHelper) {
        super(configurations, adapterFactory, serverConfiguration, watchersManager, jsonPatchHelper);
    }

    /**
     * Loads the resource with the provided {@code modelURI} and registers its {@link EPackage}s.
     * <p>
     * This method looks for {@link EPackage} instances in the loaded resource and registers them in the global EMF
     * registry. This allows to load other models that rely on these {@link EPackage}s (e.g. instances of a metamodel).
     *
     * @param modelURI the URI of the resource to load
     * @return the loaded resource
     */
    @Override
    public Optional<Resource> loadResource(String modelURI) {
        Optional<Resource> optionalResource = super.loadResource(modelURI);
        /*
         * Warning: it is not necessary to do this for every single resource we load. We should differentiate
         * metamodel resources from model resources to avoid traversing potentially large models.
         */
        optionalResource.ifPresent(this::registerEPackages);
        return optionalResource;
    }

    /**
     * Loads the metamodels and models contained in {@code directoryPath}.
     * <p>
     * This method first looks for metamodel files in the content of the provided directory, loads them, and
     * registers their {@link EPackage}s (see {@link #loadResource(String)}). It then looks for models and loads
     * them, allowing to load models with dependencies on metamodels from the same directory.
     *
     * @param directoryPath the path of the directory containing the metamodels and models to load
     */
    @Override
    protected void loadSourceResources(String directoryPath) {
        FileFilter isMetamodelFile = f -> f.getName().endsWith(".ecore");
        loadFilteredSourceResources(directoryPath, isMetamodelFile);
        /*
         * We can't use predicate negation here because FileFilter does not extend Predicate (it was introduced in
         * Java 1.2).
         */
        FileFilter isModelFile = f -> !f.getName().endsWith(".ecore");
        loadFilteredSourceResources(directoryPath, isModelFile);
    }

    /**
     * Loads the resources of the provided {@code directoryPath} that match the provided {@code fileFilter}.
     *
     * @param directoryPath the path of the directory containing the resources to load
     * @param fileFilter    the filter used to select the resources to load
     */
    private void loadFilteredSourceResources(String directoryPath, FileFilter fileFilter) {
        if (directoryPath == null || directoryPath.isEmpty()) {
            return;
        }
        File directory = new File(directoryPath);
        File[] directoryFiles = directory.listFiles(fileFilter);
        if (directoryFiles == null) {
            throw new RuntimeException("Cannot access content of directory " + directoryPath);
        }
        for (File file : directoryFiles) {
            if (isSourceDirectory(file)) {
                loadSourceResources(file.getAbsolutePath());
            } else if (file.isFile()) {
                URI modelURI = createURI(file.getAbsolutePath());
                resourceSets.put(modelURI, resourceSetFactory.createResourceSet(modelURI));
                loadResource(modelURI.toString());
            }
        }
    }

    /**
     * Registers the {@link EPackage}s contained in the provided {@code resource}.
     * <p>
     * This method traverses the entire containment tree of the provided {@code resource} and adds to the global EMF
     * registry all the {@link EPackage} instances it contains.
     *
     * @param resource the {@link Resource} to register the {@link EPackage}s from
     */
    private void registerEPackages(Resource resource) {
        Iterable<EObject> allContents = resource::getAllContents;
        for (EObject eObject : allContents) {
            if (EcorePackage.eINSTANCE.getEPackage().isInstance(eObject)) {
                EPackage ePackage = (EPackage) eObject;
                EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
                /*
                 * Hot fix: this changes the URI of metamodel resources to the NsURI of their EPackage (if any). This
                 *  is not a generic solution: it will fail if the model contains multiple EPackages.
                 */
                resource.setURI(URI.createURI(ePackage.getNsURI()));
                log.info("EPackage {} registered", ePackage.getNsURI());
            }
        }
    }
}
