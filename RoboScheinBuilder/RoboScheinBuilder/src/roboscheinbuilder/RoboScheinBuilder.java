package roboscheinbuilder;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mat
 */
public class RoboScheinBuilder {

    public RoboScheinBuilder() {

    }

    private Document setValueInPdf(String name, String value, Document doc) {
        try {
            Element  blockList = doc.getRootElement();
            Element[] e = new Element[1];
            getAttributeById(blockList, name, e);

            e[0].setText(value);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("end setCandidateNameInPDF()");
        return doc;
    }

    // Recursive method to traverse elements
    public void getAttributeById(Element element, String targetId, Element[] e) {
        // Check if the current element is a 'fo:block' and has the correct 'id' attribute
        if(!element.getAttributes().isEmpty() && element.getAttributeValue("id") != null)
        {
            String val = element.getAttributeValue("id");

            if(val.equals(targetId))
            {
                var attributes = element.getAttributes();
                e[0] = element;
            }
        }

        // If the current element has child elements, recursively traverse them
        List<Element> childNodes = element.getChildren();
        for (int i = 0; i < childNodes.size(); i++) {
            Element childNode = childNodes.get(i);

            getAttributeById(childNode, targetId, e);
        }
    }

    private Document setPhotoPathInPdf(String name, String path, Document doc) {
        try {
            Element  blockList = doc.getRootElement();
            Element[] e = new Element[1];
            getAttributeById(blockList, name, e);

            e[0].setAttribute("src", path);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("end setCandidateNameInPDF()");
        return doc;
    }

    public String generatePDF(String pfad, String vorname, String nachname, String geburtsdatum, String ausstellungsdatum, String path) {

        FopFactory fopFactory = FopFactory.newInstance();
        OutputStream out = null;
        String xmlPath = pfad+"\\roboschein.xfd";
        String pdfPath = pfad+"\\roboschein.pdf";
        try {
            Document doc = new SAXBuilder().build(xmlPath);
            XMLOutputter xmlOutputter = new XMLOutputter();

            doc = this.setValueInPdf("vorname", vorname.toUpperCase(), doc);
            doc = this.setValueInPdf("nachname", nachname.toUpperCase(), doc);
            doc = this.setValueInPdf("geburtsdatum", geburtsdatum, doc);
            doc = this.setValueInPdf("ausstellungsdatum", ausstellungsdatum, doc);

            if(!path.isEmpty())
               doc = this.setPhotoPathInPdf("foto", path, doc);

            out = new BufferedOutputStream(new FileOutputStream(new File(pdfPath)));
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
            xmlOutputter.output(doc, byteArrayOut);
            Source src = new StreamSource(new ByteArrayInputStream(byteArrayOut.toByteArray()));
            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);
        }
        catch (Exception ex) {
            return "Fehler beim schreiben der Datei!";
        }
        finally {
            try {
                out.close();
                return "Erfolgreich!";
            }
            catch (IOException ex) {
                return "Fehler beim schreiben der Datei!";
            }
            catch (Exception ex) {
                return "Fehler beim schreiben in die Datei!";
            }
        }
    }
}