/*
 * This software is in the public domain under CC0 1.0 Universal plus a 
 * Grant of Patent License.
 * 
 * To the extent possible under law, the author(s) have dedicated all
 * copyright and related and neighboring rights to this software to the
 * public domain worldwide. This software is distributed without any
 * warranty.
 * 
 * You should have received a copy of the CC0 Public Domain Dedication
 * along with this software (see the LICENSE.md file). If not, see
 * <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package github.chunlinyao.jod

import groovy.transform.CompileStatic
import org.jodconverter.core.DocumentConverter
import org.jodconverter.core.office.OfficeManager
import org.jodconverter.core.office.OfficeUtils
import org.jodconverter.local.LocalConverter
import org.jodconverter.local.office.LocalOfficeManager
import org.jodconverter.local.office.LocalOfficeUtils
import org.jodconverter.remote.RemoteConverter
import org.jodconverter.remote.office.RemoteOfficeManager
import org.moqui.context.ExecutionContextFactory
import org.moqui.context.ToolFactory
import org.moqui.util.SystemBinding
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Apache FOP tool factory to get a org.xml.sax.ContentHandler instance to write to an OutputStream with the given
 * contentType (like "application/pdf").
 */
@CompileStatic
class JodConverterToolFactory implements ToolFactory<DocumentConverter> {
    protected final static Logger logger = LoggerFactory.getLogger(JodConverterToolFactory.class)
    final static String TOOL_NAME = "JodConverter"

    protected ExecutionContextFactory ecf = null

    OfficeManager officeManager
    DocumentConverter documentConverter
    volatile boolean started = false
    /** Default empty constructor */
    JodConverterToolFactory() {}

    @Override
    String getName() { return TOOL_NAME }

    @Override
    void init(ExecutionContextFactory ecf) {
        this.ecf = ecf
    }

    void ensureStarted() {
        if (!started) {
            // remote office url
            synchronized(this) {
                if (!started) {
                    try {
                        String remoteOfficeUrl = SystemBinding.getPropOrEnv("REMOTE_OFFICE_URL")
                        if (!remoteOfficeUrl) {
                            officeManager = LocalOfficeManager.install()
                            officeManager.start()
                            documentConverter = LocalConverter.make(officeManager)
                        } else {
                            officeManager = RemoteOfficeManager.make(remoteOfficeUrl)
                            officeManager.start()
                            documentConverter = RemoteConverter.make(officeManager)
                        }
                    } finally {
                        started = true
                    }
                }
            }
        }
    }
    @Override
    void preFacadeInit(ExecutionContextFactory ecf) {}

    /** Requires 2 parameters: OutputStream out, String contentType */
    @Override
    DocumentConverter getInstance(Object... parameters) {
        ensureStarted()
        return documentConverter
    }

    @Override
    void destroy() {
        OfficeUtils.stopQuietly(officeManager)
    }

    ExecutionContextFactory getEcf() { return ecf }

}
