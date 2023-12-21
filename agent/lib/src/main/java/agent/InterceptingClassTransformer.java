package agent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.runtime.Desc;
import javassist.scopedpool.ScopedClassPoolFactoryImpl;
import javassist.scopedpool.ScopedClassPoolRepositoryImpl;
import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InterceptingClassTransformer implements ClassFileTransformer {

    private static final Logger log = Logger.getLogger(InterceptingClassTransformer.class.getName());
    private ScopedClassPoolFactoryImpl scopedClassPoolFactory = new ScopedClassPoolFactoryImpl();

    private ClassPool rootPool;

    public void init() {

        //Sets the useContextClassLoader =true to get any class type to be correctly resolved with correct OSGI module
        Desc.useContextClassLoader = true;
        rootPool = ClassPool.getDefault();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {

       
        byte[] byteCode = classfileBuffer;
 
        if (className.startsWith("runme") && !className.endsWith("App")) {
            log.info("Transforming the class " + className);
            try {
                ClassPool classPool = scopedClassPoolFactory.create(loader, rootPool,
                        ScopedClassPoolRepositoryImpl.getInstance());
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
               
                CtMethod[] methods = ctClass.getDeclaredMethods();

                for (CtMethod method : methods) {
                    method.addLocalVariable("injectedstartTime", CtClass.longType);
                    CtClass span = classPool.get("io.opentelemetry.api.trace.Span");
                    method.addLocalVariable("span", span);
                    CtClass tracerCtClass = classPool.get("io.opentelemetry.api.trace.Tracer");
                    method.addLocalVariable("tracer", tracerCtClass);
            
                    method.insertBefore("tracer = io.opentelemetry.api.GlobalOpenTelemetry.getTracer(\"test\"); " +
                    "span = tracer.spanBuilder(\"" +  method.getLongName()  +"\").setParent(io.opentelemetry.context.Context.current()).startSpan();" +
                    "injectedstartTime = System.currentTimeMillis();");

                    method.insertAfter("System.out.println(\"Execution time (ms): " + method.getLongName() + " \" + (System.currentTimeMillis() - injectedstartTime)); " +
                    "span.end();");

                  
                }
                byteCode = ctClass.toBytecode();
                ctClass.detach();
            } catch (Throwable ex) {
                log.log(Level.SEVERE, "Error in transforming the class: " + className, ex);
            }
        }
        return byteCode;
    } 
}
