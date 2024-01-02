package agent;

 
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.security.ProtectionDomain;

 
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;

public class BuddyTransformer implements Transformer {
 

    @Override
    public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
            JavaModule module, ProtectionDomain protectionDomain) {
                
                  final AsmVisitorWrapper methodsVisitor =
          Advice.to(EnterAdvice.class, ExitAdviceMethods.class)
            .on(ElementMatchers.isMethod());

                  return builder.visit(methodsVisitor);
    }

 
}
 