package ru.finam.bustard.codegen;

import com.google.gwt.core.ext.*;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.apache.commons.io.FilenameUtils;
import ru.finam.bustard.ExecuteQualifier;
import ru.finam.bustard.Executor;
import ru.finam.bustard.gwt.AbstractGwtBustard;

import java.io.PrintWriter;

public class BustardGwtGenerator extends Generator implements Consts {

    private static final String PACKAGE_NAME = "ru.finam.bustard";
    private static final String IMPL_NAME = "BustardGwtImpl";

    @Override
    public String generate(TreeLogger logger,
                           GeneratorContext context,
                           String typeName) throws UnableToCompleteException {
        PrintWriter writer = context.tryCreate(logger, PACKAGE_NAME, IMPL_NAME);

        FilenameUtils.getExtension();


        if (writer != null) {
            try {
                TypeOracle typeOracle = context.getTypeOracle();
                BustardEmitter bustardEmitter = new BustardEmitter(PACKAGE_NAME, IMPL_NAME, AbstractGwtBustard.class);
                for (MethodDescription description : ListenersFinder.retrieveSubscribeMethods()) {
                    String executeQualifierName = description.getExecuteQualifierName();

                    if (executeQualifierName != null &&
                            typeOracle.findType(executeQualifierName) != null) {
                        bustardEmitter.addExecutor(executeQualifierName, extractExecutorName(executeQualifierName));
                    } else if (executeQualifierName != null) {
                        description.setExecuteQualifierName(null);
                    }

                    if (typeOracle.findType(description.getListenerName()) != null &&
                            typeOracle.findType(description.getEventName()) != null) {
                        bustardEmitter.addSubscriber(description);
                    }
                }
                bustardEmitter.emit(writer);
            } catch (Exception e) {
                logger.log(TreeLogger.Type.ERROR, e.toString());
                throw new UnableToCompleteException();
            }
            context.commit(logger, writer);
        }
        return PACKAGE_NAME + "." + IMPL_NAME;
    }

    private String extractExecutorName(String executeQualifierName) throws ClassNotFoundException {
        Class<?> qualifierType = AbstractGwtBustard.class.getClassLoader().loadClass(executeQualifierName);
        Class<? extends Executor> executorType = qualifierType.getAnnotation(ExecuteQualifier.class).value();
        return executorType.getCanonicalName();
    }
}
