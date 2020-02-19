import lombok.Cleanup;
import lombok.val;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class Run {

    static final int SIZE_POOL = 800;
    static final ExecutorService EXEC = Executors.newFixedThreadPool(SIZE_POOL);

    static final int TRYES = 5;
    static final int QT = TRYES*Urls.size();

    static final AtomicInteger DONE = new AtomicInteger(0);
    static final AtomicInteger DONE_ERROR = new AtomicInteger(0);
    static final AtomicInteger DONE_SUCC = new AtomicInteger(0);

    public static void main(String args[]) throws InterruptedException {
        val init = new Date();
        System.out.println("--------- Início "+init+" ---------");

        for (int n = 0; n < TRYES; n++) {
            for (int i = 0; i < Urls.size(); i++) {
                val index = i;
                EXEC.execute(new Runnable() {
                    public void run(){
                        val tries = DONE.incrementAndGet();
                        try {
                            // Create a trust manager that does not validate certificate chains
                            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                    return null;
                                }
                                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                                }
                                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                                }
                            }
                            };

                            // Install the all-trusting trust manager
                            SSLContext sc = SSLContext.getInstance("SSL");
                            sc.init(null, trustAllCerts, new java.security.SecureRandom());
                            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                            // Create all-trusting host name verifier
                            HostnameVerifier allHostsValid = new HostnameVerifier() {
                            public boolean verify(String hostname, SSLSession session) {
                                return true;
                            }
                            };

                            // Install the all-trusting host verifier
                            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

                            val url = new URL(Urls.get(index));
                            val connection = url.openConnection();
                            @Cleanup val is = connection.getInputStream();

                            val buffer = new byte[8 * 1024];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                            }
                            DONE_SUCC.incrementAndGet();
                        }catch (Exception e){
                            DONE_ERROR.incrementAndGet();
                            System.out.println("Erro na requisição "+tries+": "+e.getMessage());
                        }
                    }
                });
            }
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
