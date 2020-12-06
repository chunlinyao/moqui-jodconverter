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
package github.chunlinyao.jod.resource

import github.chunlinyao.jod.JodConverterToolFactory
import groovy.transform.CompileStatic
import org.apache.commons.io.FilenameUtils
import org.jodconverter.core.DocumentConverter
import org.jodconverter.core.document.DefaultDocumentFormatRegistry
import org.moqui.entity.EntityValue
import org.moqui.impl.context.ExecutionContextFactoryImpl
import org.moqui.impl.context.reference.DbResourceReference
import org.moqui.resource.ResourceReference
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// NOTE: IDE says this isn't needed but compiler requires it

@CompileStatic
class ConvertToPdfDbResourceReference extends DbResourceReference {
    protected final static Logger logger = LoggerFactory.getLogger(ConvertToPdfDbResourceReference.class)
    public static final String PREFIX = "jod+pdf+"
    public final static String locationPrefix = PREFIX + "dbresource://"

    ConvertToPdfDbResourceReference() {}

    @Override
    ResourceReference init(String location, ExecutionContextFactoryImpl ecf) {
        int end = location.indexOf('?')
        if (end > -1) {
            end = end - 1
        }
        super.init(location ? location[PREFIX.length()..end] : location, ecf)
        return this
    }

    ResourceReference init(String location, EntityValue dbResource, ExecutionContextFactoryImpl ecf) {
        super.init(location ? location[1..-1] : location, dbResource, ecf)
        return this
    }

    @Override
    ResourceReference createNew(String location) {
        throw new UnsupportedOperationException("WaterMark resource not support create new.")
    }

    @Override
    String getFileName() {
        if (isSupportedFile()) {
            return FilenameUtils.getBaseName(super.getFileName()) + 'pdf'
        } else {
            return super.getFileName()
        }
    }

    @Override
    InputStream openStream() {
        if (isSupportedFile()) {
            return convertedStream(super.openStream())
        } else {
            return super.openStream()
        }
    }

    @Override
    boolean supportsSize() { true }

    @Override
    long getSize() {
        return super.getSize()
    }

    @Override
    InputStream openStream(String versionName) {
        if (isSupportedFile()) {
            def originStream = super.openStream(versionName)
            return convertedStream(originStream)
        } else {
            return super.openStream(versionName)
        }
    }

    private InputStream convertedStream(InputStream originStream) {
        def format = DefaultDocumentFormatRegistry.getFormatByExtension(FilenameUtils.getExtension(super.getFileName()))
        DocumentConverter converter = ecf.getTool(JodConverterToolFactory.getTOOL_NAME(), DocumentConverter)
        def stream = new ByteArrayOutputStream()
        converter.convert(originStream).as(format).to(stream).as(DefaultDocumentFormatRegistry.PDF).execute()
        return new ByteArrayInputStream(stream.toByteArray())
    }

    @Override
    String getContentType() {

        if (isSupportedFile()) {
            return "application/pdf"
        }
        return super.getContentType()
    }

    private boolean isSupportedFile() {
        def extension = FilenameUtils.getExtension(super.getFileName())
        return (extension != null) && extension.toLowerCase() in ['docx', 'xlsx', 'doc', 'xls', 'ppt', 'pptx']
    }

    @Override
    long getLastModified() {
        return super.getLastModified()
    }
}
