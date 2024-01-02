
package runme;
import reactor.core.publisher.Mono;

class Another {
    Mono<String> Run() throws InterruptedException{
   
        return Mono.just("Hello from runme.Another")
        .filter(z->z.equals("Hello from runme.Another"))
        .doOnNext(z-> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        })
        //.flatMap(z-> Mono.error( new RuntimeException("test")))
        .zipWith(Mono.just("World"))
        .map(z->z.getT1() + " " + z.getT2())
        .map(Another::LowerIt)
        .map(String::toUpperCase)
                .map(Another::LowerIt);
    }

    static String LowerIt(String s) {
        return s.toLowerCase();
    }
}
