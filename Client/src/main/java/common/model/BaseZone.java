// Paquete comun del modelo: contiene los datos topologicos basicos de una zona compartidos entre cliente y servidor.
package common.model;

/**
 * Datos topologicos basicos de una zona, compartidos con el servidor.
 */
// Clase abstracta que sirve de plantilla para las zonas del cliente (herencia).
public abstract class BaseZone {

    // Identificador de la zona.
    protected String zoneId;
    // Indica si la zona tiene bandera.
    protected boolean hasFlag;

    // Constructor protegido.
    protected BaseZone(String zoneId, boolean hasFlag) {
        // Asigna el id.
        this.zoneId = zoneId;
        // Asigna hasFlag.
        this.hasFlag = hasFlag;
    }

    // Getter del id.
    public String getZoneId() {
        // Retorna zoneId.
        return zoneId;
    }

    // Setter del id.
    public void setZoneId(String zoneId) {
        // Asigna zoneId.
        this.zoneId = zoneId;
    }

    // Indica si tiene bandera.
    public boolean hasFlag() {
        // Retorna hasFlag.
        return hasFlag;
    }

    // Setter de hasFlag.
    public void setHasFlag(boolean hasFlag) {
        // Asigna hasFlag.
        this.hasFlag = hasFlag;
    }
}