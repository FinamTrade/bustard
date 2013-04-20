package ru.finam.bustard.codegen;

import com.google.gwt.core.ext.*;

import java.io.PrintWriter;
import java.util.Set;
import java.util.TreeSet;

public class ChannelModuleGwtGenerator extends IncrementalGenerator {

    @Override
    public RebindResult generateIncrementally(TreeLogger logger,
                                              GeneratorContext context,
                                              String typeName) throws UnableToCompleteException {
        String packageName = ChannelModule.class.getPackage().getName();
        String simpleName = ChannelModule.class.getName();

        PrintWriter writer = context.tryCreate(logger, packageName, simpleName);

        Set<String> channelKeys = new TreeSet<String>();
        if (writer != null) {
            try {
                ChannelModuleGenerator generator = new ChannelModuleGenerator();
                for (String key : ChannelsFinder.retrieveChannelKeys()) {
                    channelKeys.add(key);
                }
                generator.generate(channelKeys, writer);
            } catch (Exception e) {
                logger.log(TreeLogger.Type.ERROR, e.toString());
                throw new UnableToCompleteException();
            }
            context.commit(logger, writer);
        }
        return new RebindResult(RebindMode.USE_ALL_NEW_WITH_NO_CACHING, packageName + "." + simpleName);
    }

    @Override
    public long getVersionId() {
        return 0;
    }
}