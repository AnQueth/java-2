package agent;

import java.lang.instrument.Instrumentation;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

public class JavaAgent {


    public static void premain(String agentArgs, Instrumentation instrumentation) throws InstantiationException {

/*         InterceptingClassTransformer interceptingClassTransformer = new InterceptingClassTransformer();

        interceptingClassTransformer.init();

        instrumentation.addTransformer(interceptingClassTransformer); */
 
 

                new AgentBuilder.Default()
                .type(ElementMatchers.nameStartsWithIgnoreCase("runme"))
                .transform(new BuddyTransformer())
               // .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE)
                .installOn(instrumentation);


    }

}