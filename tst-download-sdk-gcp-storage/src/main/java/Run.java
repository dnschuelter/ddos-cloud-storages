import lombok.Cleanup;
import lombok.val;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Run {

    static final String BUCKET_NAME = "";
    static final String SRC_FILE = "";

    static final int SIZE_POOL = 5 * 1000;
    static final ExecutorService EXEC = Executors.newFixedThreadPool(SIZE_POOL);

    static final int QT = 1000 * 1000;
    static final AtomicInteger DONE = new AtomicInteger(0);
    static final AtomicInteger DONE_ERROR = new AtomicInteger(0);
    static final AtomicInteger DONE_SUCC = new AtomicInteger(0);

    static final Storage storage = StorageOptions.getDefaultInstance().getService();

    public static void main(String args[]) throws InterruptedException {
        val init = new Date();
        System.out.println("--------- Início "+init+" ---------");

        for (int i = 0; i < QT; i++) {
            EXEC.execute(() -> {
                try {
                    Blob blob = storage.get(BlobId.of(BUCKET_NAME, SRC_FILE));

                    DONE_SUCC.incrementAndGet();
                }catch (Exception e){
                    DONE_ERROR.incrementAndGet();
                    System.out.println("Erro na requisição "+DONE.get()+": "+e.getMessage());
                }
                val tries = DONE.incrementAndGet();
            });
        }

        while(DONE.get() < QT){
            Thread.sleep(60*1000);
            System.out.println("waiting ... "+DONE.get()+"/"+QT);
        }
        Thread.sleep(30*1000);
        System.out.println("--------- Fim (de: "+init+" até: "+new Date()+") ---------");
        System.out.println("--------- ERROR "+DONE_ERROR.get()+" ---------");
        System.out.println("--------- SUCC "+DONE_SUCC.get()+" ---------");
        System.exit(0);
    }
}
