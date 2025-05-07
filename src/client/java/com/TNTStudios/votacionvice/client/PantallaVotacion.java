package com.TNTStudios.votacionvice.client;

import net.minecraft.client.gui.DrawContext;
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

        int pantallaAncho = this.width;
        int pantallaAlto = this.height;

        // Base sizes
        int buttonWidth = pantallaAncho < 300 ? 16 : 20;
        int buttonGap = pantallaAncho < 300 ? 2 : 4;
        int totalButtonWidth = (buttonWidth + buttonGap) * 10 - buttonGap;
        int centerX = pantallaAncho / 2 - totalButtonWidth / 2;

        int defaultSpacingY = 55;
        int defaultStartY = 30;
        int buttonHeight = 20;

        // Estimar altura total requerida
        int totalHeight = defaultStartY + categorias.size() * defaultSpacingY + buttonHeight + 20;
        int spacingY = defaultSpacingY;
        int startY = defaultStartY;

        // Reducir tamaño si no cabe
        if (totalHeight > pantallaAlto) {
            spacingY = 42;
            startY = 20;
        }

        // Título centrado
        addDrawableChild(new TextWidget(
                pantallaAncho / 2 - 100,
                10,
                200, 16,
                Text.literal("Votación de equipo " + equipo).setStyle(Style.EMPTY.withBold(true).withColor(Formatting.GOLD)),
                this.textRenderer
        ));

        for (int i = 0; i < categorias.size(); i++) {
            String categoria = categorias.get(i);
            int yBase = startY + (i * spacingY);

            addDrawableChild(new TextWidget(
                    pantallaAncho / 2 - 100,
                    yBase,
                    200, 12,
                    Text.literal(categoria).setStyle(Style.EMPTY.withBold(true).withColor(Formatting.YELLOW)),
                    this.textRenderer
            ));

            List<ButtonWidget> botones = new ArrayList<>();

            for (int j = 1; j <= 10; j++) {
                int x = centerX + (j - 1) * (buttonWidth + buttonGap);
                int y = yBase + 15;
                int score = j;

                ButtonWidget btn = ButtonWidget.builder(
                        Text.literal(String.valueOf(j)).formatted(Formatting.GRAY),
                        b -> {
                            respuestas.put(categoria, score);
                            actualizarColoresBotones(categoria, score);
                        }
                ).position(x, y).size(buttonWidth, buttonHeight).build();

                botones.add(btn);
                addDrawableChild(btn);
            }

            botonesPorCategoria.put(categoria, botones);
        }

        // Botón Enviar centrado
        int finalY = startY + categorias.size() * spacingY;
        addDrawableChild(ButtonWidget.builder(Text.literal("Enviar").formatted(Formatting.AQUA), btn -> {
            if (respuestas.size() == 4) {
                VotacionPacket.send(this.equipo, respuestas);
                this.client.player.sendMessage(Text.literal("¡Gracias por votar!").formatted(Formatting.GREEN));
                this.close();
            } else {
                this.client.player.sendMessage(Text.literal("Debes votar en todas las categorías.").formatted(Formatting.RED));
            }
        }).position(pantallaAncho / 2 - 40, finalY).size(80, buttonHeight).build());
    }

    private void actualizarColoresBotones(String categoria, int seleccionado) {
        List<ButtonWidget> botones = botonesPorCategoria.get(categoria);
        for (ButtonWidget btn : botones) {
            int valor = Integer.parseInt(btn.getMessage().getString());
            btn.setMessage(Text.literal(String.valueOf(valor))
                    .formatted(valor == seleccionado ? Formatting.GREEN : Formatting.GRAY));
        }
    }

    @Override
    public void renderBackground(DrawContext context) {
        context.fill(0, 0, this.width, this.height, 0xB0000000); // fondo negro
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
    }
}
