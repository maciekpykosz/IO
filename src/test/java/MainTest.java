import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    public void shouldLaunchAnApp() {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(3000);
            }
            catch (InterruptedException ignored) {
            }
            System.exit(0);
        });
        thread.start();
        Main.main(new String[]{});
    }
}