package ru.finam.bustard.codegen;

import com.google.gwt.core.ext.*;
import ru.finam.bustard.BustardImpl;
import ru.finam.bustard.ExecuteQualifier;
import ru.finam.bustard.Executor;
import ru.finam.bustard.gwt.AbstractGwtBustard;

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
                for(MethodDescription description : SubscribersFinder.retrieveSubscribeMethods()) {
                    bustardEmitter.addSubscriber(description);

                    String executeQualifierName = description.getExecuteQualifierName();
                    if (executeQualifierName != null) {
                        bustardEmitter.addExecutor(executeQualifierName, extractExecutorName(executeQualifierName));
                    }
                }
                bustardEmitter.emit(writer);
            } catch (Exception e) {
                logger.log(TreeLogger.Type.ERROR, e.toString());
                throw new UnableToCompleteException();
            }
            context.commit(logger, writer);
        }
        return new RebindResult(RebindMode.USE_ALL_NEW_WITH_NO_CACHING, PACKAGE_NAME + "." + IMPL_NAME);
    }

    private String extractExecutorName(String executeQualifierName) throws ClassNotFoundException {
        Class<?> qualifierType = BustardImpl.class.getClassLoader().loadClass(executeQualifierName);
        Class<? extends Executor> executorType = qualifierType.getAnnotation(ExecuteQualifier.class).value();
        return executorType.getCanonicalName();
    }

    @Override
    public long getVersionId() {
        return 0;
    }
}
