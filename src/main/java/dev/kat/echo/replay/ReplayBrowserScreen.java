package dev.kat.echo.replay;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.List;

public class ReplayBrowserScreen extends Screen {

    private final List<Path> recordings;

    public ReplayBrowserScreen(List<Path> recordings) {
        super(Text.literal("Echo \u2014 Replays"));
        this.recordings = recordings;
    }

    @Override
    protected void init() {
        int y = 40;
        for (int i = 0; i < recordings.size(); i++) {
            final Path p = recordings.get(i);
            String label = p.getParent().getFileName() + "/" + p.getFileName();
            this.addDrawableChild(ButtonWidget.builder(Text.literal(label), btn -> ReplayManager.openReplay(p))
                .dimensions(this.width / 2 - 160, y + i * 22, 320, 20).build());
        }
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), btn -> this.close())
            .dimensions(this.width / 2 - 60, this.height - 30, 120, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        this.renderBackground(ctx, mx, my, delta);
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, 0xFFFFFF);
        if (recordings.isEmpty())
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("No recordings found."), this.width / 2, this.height / 2, 0xAAAAAA);
        super.render(ctx, mx, my, delta);
    }
}
