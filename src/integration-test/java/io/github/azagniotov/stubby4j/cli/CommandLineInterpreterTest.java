package io.github.azagniotov.stubby4j.cli;

import io.github.azagniotov.stubby4j.utils.StringUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static com.google.common.truth.Truth.assertThat;
import static io.github.azagniotov.stubby4j.utils.FileUtils.BR;


public class CommandLineInterpreterTest {


    @Test
    public void consolePrintedHelpMessageShouldBeAsExpected() throws Exception {

        final ByteArrayOutputStream consoleCaptor = new ByteArrayOutputStream();
        final boolean NO_AUTO_FLUSH = false;
        final PrintStream oldPrintStream = System.out;
        System.setOut(new PrintStream(consoleCaptor, NO_AUTO_FLUSH, StringUtils.UTF_8));

        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.printHelp();

        System.setOut(oldPrintStream);

        final String expectedConsoleOutput = "usage:" + BR +
                "java -jar stubby4j-x.x.xx.jar [-a <arg>] [-d <arg>] [-da] [-dc] [-ds] [-h]" + BR +
                "       [-k <arg>] [-l <arg>] [-m] [-o] [-p <arg>] [-s <arg>] [-t <arg>]" + BR +
                "       [-ta] [-v] [-w <arg>]" + BR +
                " -a,--admin <arg>                        Port for admin portal. Defaults" + BR +
                "                                         to 8889." + BR +
                " -d,--data <arg>                         Data file to pre-load endpoints." + BR +
                "                                         Data file to pre-load endpoints." + BR +
                "                                         Optional valid YAML 1.1 is" + BR +
                "                                         expected. If YAML is not" + BR +
                "                                         provided, you will be expected to" + BR +
                "                                         configure stubs via the stubby4j" + BR +
                "                                         HTTP POST API." + BR +
                " -da,--disable_admin_portal              Does not start Admin portal" + BR +
                " -dc,--disable_stub_caching              Since v7.2.0. Disables stubs" + BR +
                "                                         in-memory caching when stubs are" + BR +
                "                                         successfully matched to the" + BR +
                "                                         incoming HTTP requests" + BR +
                " -ds,--disable_ssl                       Disables TLS support (enabled by" + BR +
                "                                         default) and disables the" + BR +
                "                                         '--enable_tls_with_alpn_and_http_" + BR +
                "                                         2' flag, if the latter was" + BR +
                "                                         provided" + BR +
                " -h,--help                               This help text." + BR +
                " -k,--keystore <arg>                     Keystore file for custom TLS. By" + BR +
                "                                         default TLS is enabled using" + BR +
                "                                         internal self-signed certificate." + BR +
                " -l,--location <arg>                     Hostname at which to bind stubby." + BR +
                " -m,--mute                               Mute console output." + BR +
                " -o,--debug                              Dumps raw HTTP request to the" + BR +
                "                                         console (if console is not" + BR +
                "                                         muted!)." + BR +
                " -p,--password <arg>                     Password for the provided" + BR +
                "                                         keystore file." + BR +
                " -s,--stubs <arg>                        Port for stub portal. Defaults to" + BR +
                "                                         8882." + BR +
                " -t,--tls <arg>                          Port for TLS connection. Defaults" + BR +
                "                                         to 7443." + BR +
                " -ta,--enable_tls_with_alpn_and_http_2   Since v7.3.4-SNAPSHOT. Enables" + BR +
                "                                         HTTP/2 for HTTPS URIs over TLS" + BR +
                "                                         (on TLS v1.2 or newer) using ALPN" + BR +
                "                                         extension" + BR +
                " -v,--version                            Prints out to console stubby" + BR +
                "                                         version." + BR +
                " -w,--watch <arg>                        Since v2.0.11. Periodically scans" + BR +
                "                                         for changes in last modification" + BR +
                "                                         date of the main YAML and" + BR +
                "                                         referenced external files (if" + BR +
                "                                         any). The flag can accept an" + BR +
                "                                         optional arg value which is the" + BR +
                "                                         watch scan time in milliseconds." + BR +
                "                                         If milliseconds is not provided," + BR +
                "                                         the watch scans every 100ms. If" + BR +
                "                                         last modification date changed" + BR +
                "                                         since the last scan period, the" + BR +
                "                                         stub configuration is reloaded";

        final String actualConsoleOutput = consoleCaptor.toString(StringUtils.UTF_8).trim();

        assertThat(actualConsoleOutput).isEqualTo(expectedConsoleOutput);
    }
}