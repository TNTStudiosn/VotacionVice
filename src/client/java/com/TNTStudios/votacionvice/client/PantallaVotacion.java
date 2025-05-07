package com.TNTStudios.votacionvice.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class PantallaVotacion extends Screen {
    private final String equipo;
    private final Map<String, Integer> respuestas = new HashMap<>();
    private final Map<String, List<ButtonWidget>> botonesPorCategoria = new HashMap<>();

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
            int yBase = startY + (i * 50);
            addDrawableChild(new TextWidget(
                    this.width / 2 - 100,
                    yBase,
                    100, 12,
                    Text.literal(categoria).copy().setStyle(Style.EMPTY.withBold(true).withColor(Formatting.YELLOW)),
                    this.textRenderer
            ));

            List<ButtonWidget> botones = new ArrayList<>();

            for (int j = 1; j <= 10; j++) {
                int x = this.width / 2 - 110 + (j * (buttonWidth + 2));
                int y = yBase + 15; 
                int score = j;

                ButtonWidget btn = ButtonWidget.builder(
                        Text.literal(String.valueOf(j)).formatted(Formatting.GRAY),
                        b -> {
                            respuestas.put(categoria, score);
                            actualizarColoresBotones(categoria, score);
                        }
                ).position(x, y).size(buttonWidth, 20).build();

                botones.add(btn);
                addDrawableChild(btn);
            }

            botonesPorCategoria.put(categoria, botones);
        }

        // Botón enviar
        addDrawableChild(ButtonWidget.builder(Text.literal("Enviar").formatted(Formatting.AQUA), btn -> {
            if (respuestas.size() == 4) {
                VotacionPacket.send(this.equipo, respuestas);
                this.client.player.sendMessage(Text.literal("¡Gracias por votar!").formatted(Formatting.GREEN));
                this.close();
            } else {
                this.client.player.sendMessage(Text.literal("Debes votar en todas las categorías.").formatted(Formatting.RED));
            }
        }).position(this.width / 2 - 40, startY + 220).size(80, 20).build());
    }

    private void actualizarColoresBotones(String categoria, int seleccionado) {
        List<ButtonWidget> botones = botonesPorCategoria.get(categoria);
        for (ButtonWidget btn : botones) {
            int valor = Integer.parseInt(btn.getMessage().getString());
            btn.setMessage(Text.literal(String.valueOf(valor))
                    .formatted(valor == seleccionado ? Formatting.GREEN : Formatting.GRAY));
        }
    }
}
