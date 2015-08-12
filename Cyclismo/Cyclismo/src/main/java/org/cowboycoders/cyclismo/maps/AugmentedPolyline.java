package org.cowboycoders.cyclismo.maps;

import org.mapsforge.core.graphics.Color;
import org.mapsforge.map.layer.overlay.Polyline;

/**
 * Augmented with paint color
 * Created by fluxoid on 18/03/15.
 */
public class AugmentedPolyline {

    private Color color;
    private Polyline polyLine;

    public AugmentedPolyline(Polyline polyLine, Color color) {
        if (color == null || polyLine == null) {
            throw new NullPointerException("polyline/color may not be null");
        }
        this.polyLine = polyLine;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public Polyline getPolyLine() {
        return polyLine;
    }

}
