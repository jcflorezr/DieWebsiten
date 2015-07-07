
package proxy.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the proxy.types package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _PuntoEntrada_QNAME = new QName("http://WebServices.modelo.diewebsiten.com/", "puntoEntrada");
    private final static QName _PuntoEntradaResponse_QNAME = new QName("http://WebServices.modelo.diewebsiten.com/", "puntoEntradaResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: proxy.types
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PuntoEntradaResponse }
     * 
     */
    public PuntoEntradaResponse createPuntoEntradaResponse() {
        return new PuntoEntradaResponse();
    }

    /**
     * Create an instance of {@link PuntoEntrada }
     * 
     */
    public PuntoEntrada createPuntoEntrada() {
        return new PuntoEntrada();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PuntoEntrada }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://WebServices.modelo.diewebsiten.com/", name = "puntoEntrada")
    public JAXBElement<PuntoEntrada> createPuntoEntrada(PuntoEntrada value) {
        return new JAXBElement<PuntoEntrada>(_PuntoEntrada_QNAME, PuntoEntrada.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PuntoEntradaResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://WebServices.modelo.diewebsiten.com/", name = "puntoEntradaResponse")
    public JAXBElement<PuntoEntradaResponse> createPuntoEntradaResponse(PuntoEntradaResponse value) {
        return new JAXBElement<PuntoEntradaResponse>(_PuntoEntradaResponse_QNAME, PuntoEntradaResponse.class, null, value);
    }

}
