package ru.finam.bustard.codegen;

import com.google.gwt.core.ext.*;
import ru.finam.bustard.gwt.AbstractGwtBustard;

import java.io.IOException;
import java.io.PrintWriter;

public class BustardGwtGenerator extends IncrementalGenerator {

    private static final String PACKAGE_NAME = "ru.finam.bustard";
    private static final String IMPL_NAME = "BustardGwtImpl";

    @Override
    public RebindResult generateIncrementally(TreeLogger logger,
                                              GeneratorContext context,
                                              String typeName) throws UnableToCompleteException {
        PrintWriter writer = context.tryCreate(logger, PACKAGE_NAME, IMPL_NAME);
        if (writer != null) {
            try {
                BustardEmitter bustardEmitter = new BustardEmitter(PACKAGE_NAME, IMPL_NAME, AbstractGwtBustard.class);
                for(SubscriberInfo info : SubscribersFinder.retrieveSubscribersInfo()) {
                    bustardEmitter.addSubscriber(
                            info.getEventName(),
                            info.getSubscriberName(),
                            info.getMethodName());
                }
                bustardEmitter.emit(writer);
            } catch (IOException e) {
                logger.log(TreeLogger.Type.ERROR, e.toString());
                throw new UnableToCompleteException();
            }
            context.commit(logger, writer);
        }
        return new RebindResult(RebindMode.USE_ALL_NEW_WITH_NO_CACHING, PACKAGE_NAME + "." + IMPL_NAME);
    }

    @Override
    public long getVersionId() {
        return 0;
    }
}
