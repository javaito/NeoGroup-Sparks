
package org.neogroup.sparks;

import org.neogroup.sparks.commands.Command;
import org.neogroup.sparks.processors.Processor;
import org.neogroup.sparks.processors.ProcessorComponent;
import org.neogroup.sparks.processors.ProcessorException;
import org.neogroup.sparks.processors.ProcessorNotFoundException;
import org.neogroup.sparks.properties.Properties;
import org.neogroup.sparks.views.View;
import org.neogroup.sparks.views.ViewException;
import org.neogroup.sparks.views.ViewFactory;
import org.neogroup.sparks.views.ViewNotFoundException;

import javax.sql.DataSource;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

public abstract class ApplicationContext {

    private static final String DEFAULT_VIEW_FACTORY_PROPERTY = "defaultViewFactory";
    private static final String DEFAULT_DATA_SOURCE_PROPERTY = "defaultDataSource";

    protected boolean running;
    protected Properties properties;
    protected Logger logger;
    protected final Map<String, DataSource> dataSources;
    protected final Map<String, ViewFactory> viewFactories;
    protected final Set<Class<? extends Processor>> registeredProcessors;
    protected final Map<Class<? extends Command>, Class<? extends Processor>> processorsByCommand;
    protected final Map<Class<? extends Processor>, Processor> singleInstanceProcessors;

    public ApplicationContext() {
        running = false;
        this.dataSources = new HashMap<>();
        this.viewFactories = new HashMap<>();
        this.registeredProcessors = new HashSet<>();
        this.processorsByCommand = new HashMap<>();
        this.singleInstanceProcessors = new HashMap<>();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public final void registerProcessors (Class<? extends Processor> ... processorClasses) {
        for (Class<? extends Processor> processorClass : processorClasses) {
            registerProcessor(processorClass);
        }
    }

    public final void registerProcessor (Class<? extends Processor> processorClass) {
        registeredProcessors.add(processorClass);
    }

    public Set<Class<? extends Processor>> getRegisteredProcessors() {
        return registeredProcessors;
    }

    public Processor getProcessorInstance (Class<? extends Processor> processorClass) {
        Processor processor = singleInstanceProcessors.get(processorClass);
        if (processor == null) {
            if (registeredProcessors.contains(processorClass)) {
                try {
                    processor = processorClass.newInstance();
                    processor.setApplicationContext(this);
                    processor.initialize();
                } catch (Exception exception) {
                    throw new ProcessorException("Error instanciating processor", exception);
                }
            }
        }
        return processor;
    }

    public <R> R processCommand(Command command) {
        Class<? extends Processor> processorClass = processorsByCommand.get(command.getClass());
        if (processorClass == null) {
            throw new ProcessorNotFoundException("Processor not found for command \"" + command.toString() + "\" !!");
        }
        return (R) getProcessorInstance(processorClass).process(command);
    }

    public void addViewFactory(String viewFactoryName, ViewFactory viewFactory) {
        viewFactories.put(viewFactoryName, viewFactory);
    }

    public void removeViewFactory(String viewFactoryName) {
        viewFactories.remove(viewFactoryName);
    }

    public View createView(String viewName) throws ViewException {

        View view = null;
        if (viewFactories.size() == 1) {
            view = createView(viewFactories.keySet().iterator().next(), viewName);
        }
        else if (getProperties().contains(DEFAULT_VIEW_FACTORY_PROPERTY)) {
            view = createView(getProperties().get(DEFAULT_VIEW_FACTORY_PROPERTY), viewName);
        }
        return view;
    }

    public View createView(String viewFactoryName, String viewName) throws ViewException {

        View view = null;
        ViewFactory viewFactory = viewFactories.get(viewFactoryName);
        if (viewFactory != null) {
            view = viewFactory.createView(viewName);
        }
        if (view == null) {
            throw new ViewNotFoundException(MessageFormat.format("View \"" + viewName + " not found !!", viewName));
        }
        return view;
    }

    public void addDataSource (String name, DataSource dataSource) {
        this.dataSources.put(name, dataSource);
    }

    public void removeDataSource (String name) {
        this.dataSources.remove(name);
    }

    public DataSource getDataSource () {
        DataSource source = null;
        if (dataSources.size() == 1) {
            source = dataSources.values().iterator().next();
        }
        else if (getProperties().contains(DEFAULT_DATA_SOURCE_PROPERTY)) {
            source = dataSources.get(getProperties().get(DEFAULT_DATA_SOURCE_PROPERTY));
        }
        return source;
    }

    public DataSource getDataSource (String name) {
        return dataSources.get(name);
    }

    private void startProcessors () {
        for (Class<? extends Processor> processorClass : registeredProcessors) {
            ProcessorComponent processorAnnotation = processorClass.getAnnotation(ProcessorComponent.class);
            if (processorAnnotation != null) {

                if (processorAnnotation.singleInstance()) {
                    try {
                        Processor processor = processorClass.newInstance();
                        processor.setApplicationContext(this);
                        processor.initialize();
                        singleInstanceProcessors.put(processorClass, processor);
                    } catch (Exception exception) {
                        throw new ProcessorException("Error instanciating processor", exception);
                    }
                }

                Class<? extends Command>[] commandClasses = processorAnnotation.commands();
                for (Class<? extends Command> commandClass : commandClasses) {
                    processorsByCommand.put(commandClass, processorClass);
                }
            }
        }
    }

    private void stopProcessors () {
        singleInstanceProcessors.clear();
        processorsByCommand.clear();
    }

    public final void start () {
        if (!running) {
            startProcessors();
            onStart();
            running = true;
        }
    }

    public final void stop () {
        if (running) {
            stopProcessors();
            onStop();
            running = false;
        }
    }

    protected abstract void onStart ();
    protected abstract void onStop ();
}