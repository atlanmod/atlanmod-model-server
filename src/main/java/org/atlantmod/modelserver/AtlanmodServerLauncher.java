package org.atlantmod.modelserver;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emfcloud.modelserver.emf.launch.CLIBasedModelServerLauncher;
import org.eclipse.emfcloud.modelserver.emf.launch.CLIParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AtlanmodServerLauncher {

    private static final String TEMP_DIR = ".temp";

    private static final String WORKSPACE_ROOT = "workspace";

    private static final String PROCESS_NAME = "java -jar org.atlanmod.modelserver-X.X.X-SNAPSHOT-standalone.jar";

    private static Logger log = LoggerFactory.getLogger(AtlanmodServerLauncher.class);

    private AtlanmodServerLauncher() {

    }

    public static void main(String[] args) {
        final CLIBasedModelServerLauncher launcher = new CLIBasedModelServerLauncher(
                createCLIParser(args),
                new AtlanmodServerModule());
        launcher.run();
    }

    protected static CLIParser createCLIParser(final String[] args) {
        CLIParser parser = new CLIParser(args, CLIParser.getDefaultCLIOptions(), PROCESS_NAME, 8081);
        ensureWorkspaceRoot(parser);
        return parser;
    }

    protected static void ensureWorkspaceRoot(final CLIParser parser) {
        if (!parser.optionExists(CLIParser.OPTION_WORKSPACE_ROOT)) {
            /*
             * No workspace root was specified, use test workspace.
             */
            final File workspaceRoot = new File(TEMP_DIR + File.separator + WORKSPACE_ROOT);
            if (!workspaceRoot.exists()) {
                log.error("Cannot load the workspace: {} does not exist", workspaceRoot.getAbsolutePath());
                System.exit(1);
            }
            log.info("Loading workspace {}", workspaceRoot.toURI());
            parser.setOption(CLIParser.OPTION_WORKSPACE_ROOT, workspaceRoot.toURI());
        }
    }
}
