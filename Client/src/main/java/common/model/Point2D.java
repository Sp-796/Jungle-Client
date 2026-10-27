// Paquete comun del modelo: contiene el punto inmutable del mundo.
package common.model;

// Importa Objects para hashCode.
import java.util.Objects;

/**
 * Punto inmutable del mundo (x, y).
 *
 * Se usa para destinos, centros de zona y calculos de distancia. Al ser
 * inmutable se puede compartir sin riesgo entre la interfaz y los modelos.
 */
// Clase final inmutable que representa un punto (x, y).
public final class Point2D {

    // Coordenada x inmutable.
    private final double x;
    // Coordenada y inmutable.
    private final double y;

    // Constructor.
    public Point2D(double x, double y) {
        // Asigna x.
        this.x = x;
        // Asigna y.
        this.y = y;
    }

    // Getter de x.
    public double getX() {
        // Retorna x.
        return x;
    }

    // Getter de y.
    public double getY() {
        // Retorna y.
        return y;
    }

    // Distancia euclidiana.
    public double distanceTo(Point2D other) {
        // Retorna la distancia.
        return Math.hypot(this.x - other.x, this.y - other.y);
    }

    // Avanza hacia el objetivo sin pasarse de maxDistance.
    public Point2D stepTowards(Point2D target, double maxDistance) {
        // Distancia al objetivo.
        double distance = distanceTo(target);
        // Si esta cerca, retorna el objetivo.
        if (distance <= maxDistance || distance == 0.0) {
            return target;
        }
        // Fraccion a avanzar.
        double ratio = maxDistance / distance;
        // Retorna el nuevo punto.
        return new Point2D(
                this.x + (target.x - this.x) * ratio,
                this.y + (target.y - this.y) * ratio
        );
    }

    // Compara dos puntos.
    @Override
    public boolean equals(Object other) {
        // Si es el mismo, retorna true.
        if (this == other) {
            return true;
        }
        // Si no es Point2D, retorna false.
        if (!(other instanceof Point2D point)) {
            return false;
        }
        // Compara coordenadas.
        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
    }

    // HashCode del punto.
    @Override
    public int hashCode() {
        // Usa Objects.hash.
        return Objects.hash(x, y);
    }

    // Representacion textual.
    @Override
    public String toString() {
        // Retorna "Point2D(x, y)".
        return "Point2D(" + x + ", " + y + ")";
    }
}