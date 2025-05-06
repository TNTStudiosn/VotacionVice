package com.TNTStudios.votacionvice.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PantallaVotacion extends Screen {
    private final String equipo;
    private final Map<String, Integer> respuestas = new HashMap<>();

    public PantallaVotacion(String equipo) {
        super(Text.literal("Votación de equipo " + equipo));
        this.equipo = equipo;
    }

    @Override
    protected void init() {
        List<String> categorias = List.of("Originalidad", "Decoración", "Materiales", "Semejanza a la realidad");
        int startY = 40;
        int buttonWidth = 20;

        for (int i = 0; i < categorias.size(); i++) {
            String categoria = categorias.get(i);
            addDrawableChild(new TextWidget(this.width / 2 - 100, startY + (i * 40), Text.literal(categoria)));

            for (int j = 1; j <= 10; j++) {
                int x = this.width / 2 - 100 + (j * (buttonWidth + 2));
                int y = startY + (i * 40) + 15;
                int score = j;
                addDrawableChild(ButtonWidget.builder(Text.literal(String.valueOf(j)), btn -> {
                    respuestas.put(categoria, score);
                }).position(x, y).size(buttonWidth, 20).build());
            }
        }

        // Botón Enviar
        addDrawableChild(ButtonWidget.builder(Text.literal("Enviar"), btn -> {
            if (respuestas.size() == 4) {
                NetworkHandler.sendVotacion(this.equipo, respuestas);
                this.close();
            } else {
                this.client.player.sendMessage(Text.literal("Debes votar en todas las categorías."));
            }
        }).position(this.width / 2 - 40, startY + 180).size(80, 20).build());
    }
}

