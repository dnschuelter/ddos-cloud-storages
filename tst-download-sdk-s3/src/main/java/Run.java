import lombok.Cleanup;
import lombok.val;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.services.s3.model.S3Object;

public class Run {

    static final String REGION = "";
    static final String BUCKET = "";
    static final String KEY = "";

    static final AWSStaticCredentialsProvider CREDENTIAL = new AWSStaticCredentialsProvider(new BasicAWSCredentials(
   "",
   ""));

    static final int SIZE_POOL = 5 * 1000;
    static final ExecutorService EXEC = Executors.newFixedThreadPool(SIZE_POOL);

    static final int QT = 100 * 1000;
    static final AtomicInteger DONE = new AtomicInteger(0);
    static final AtomicInteger DONE_ERROR = new AtomicInteger(0);
    static final AtomicInteger DONE_SUCC = new AtomicInteger(0);

    public static void main(String args[]) throws InterruptedException {
        val init = new Date();
        System.out.println("--------- Início "+init+" ---------");

        for (int i = 0; i < QT; i++) {
            EXEC.execute(() -> {
                val tries = DONE.incrementAndGet();
                try {
                    AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(CREDENTIAL)
                    .withRegion(Regions.US_EAST_1)
                    .build();

                    val fullObject = s3Client.getObject(new GetObjectRequest(BUCKET, KEY));

                    @Cleanup val is = fullObject.getObjectContent();

                    val buffer = new byte[8 * 1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                    }
                    DONE_SUCC.incrementAndGet();
                }catch (Exception e){
                    DONE_ERROR.incrementAndGet();
                    System.out.println("Erro na requisição "+tries+": "+e.getMessage());
                }
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
