package org.atlantmod.modelserver;

import org.eclipse.emfcloud.modelserver.common.ModelServerPathParameters;
import org.eclipse.emfcloud.modelserver.common.Routing;
import org.eclipse.emfcloud.modelserver.common.codecs.Codec;
import org.eclipse.emfcloud.modelserver.common.utils.MapBinding;
import org.eclipse.emfcloud.modelserver.common.utils.MultiBinding;
import org.eclipse.emfcloud.modelserver.emf.common.ModelResourceManager;
import org.eclipse.emfcloud.modelserver.emf.common.codecs.CodecProvider;
import org.eclipse.emfcloud.modelserver.emf.configuration.EPackageConfiguration;
import org.eclipse.emfcloud.modelserver.emf.di.DefaultModelServerModule;

import java.util.Map;

public class AtlanmodServerModule extends DefaultModelServerModule {

    @Override
    protected void configureEPackages(final MultiBinding<EPackageConfiguration> binding) {
        super.configureEPackages(binding);
    }

    @Override
    protected void configureRoutings(final MultiBinding<Routing> binding) {
        super.configureRoutings(binding);
    }

    @Override
    protected void configureCodecs(MultiBinding<CodecProvider> binding) {
        super.configureCodecs(binding);
        binding.add(CoffeeCodecProvider.class);
//        binding.add(new)Map.of("ecore", CoffeeCodec.class);
//        binding.put(ModelServerPathParameters.FORMAT_JSON, CoffeeTreeJsonCodec.class);
    }

    @Override
    protected Class<? extends ModelResourceManager> bindModelResourceManager() {
        return MetamodelAwareModelResourceManager.class;
    }
}
