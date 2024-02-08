package caleb.keep.xp;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import caleb.keep.xp.interfaces.Updateable;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

public class HitColorClient implements ClientModInitializer{

    public static final Logger LOGGER = LoggerFactory.getLogger("Hit Color");
    Path path = FabricLoader.getInstance().getConfigDir().resolve("hitcolor" + ".json");
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static String color = "#ff0000";
    public static float alpha = 32.8f;

    @Override
    public void onInitializeClient() {
        loadConfig();
        register();
    }

    @SuppressWarnings("resource")
    public void register(){
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> 
        dispatcher.register(ClientCommandManager.literal("hitcolor").then(ClientCommandManager.literal("setColor")
        .then(ClientCommandManager.argument("color", StringArgumentType.string()).executes(context -> {
            color = StringArgumentType.getString(context, "color");
            ((Updateable)context.getSource().getClient().gameRenderer.getOverlayTexture()).updateOverlay();
            saveConfig();
            return Command.SINGLE_SUCCESS;
        }))).then(ClientCommandManager.literal("setAlpha")
        .then(ClientCommandManager.argument("alpha", FloatArgumentType.floatArg()).executes(context -> {
            alpha = FloatArgumentType.getFloat(context, "alpha");
            ((Updateable)context.getSource().getClient().gameRenderer.getOverlayTexture()).updateOverlay();
            saveConfig();
            return Command.SINGLE_SUCCESS;
        })))
        ));
    }

    public void saveConfig(){
        try {
            FileWriter writer = new FileWriter(path.toString());

            gson.toJson(new Config(color, alpha), writer);
            
            writer.flush();
            LOGGER.info("Config file saved!");
            
        } 
        catch (IOException e) {
            LOGGER.info("Oh oh! Couldn't save the config file...");
        }
    }

    public void loadConfig(){
        try {
            FileReader reader = new FileReader(path.toString());

            Config config = gson.fromJson(reader, Config.class);

            color = config.color;
            alpha = config.alpha;

            reader.close();
            LOGGER.info("A config file was found! Correctly loaded configs.");
        } 
        catch (IOException e) {
            LOGGER.info("No config file was found...creating a new one!");
            saveConfig();
        }
    }

    private class Config{
        public String color;
        public float alpha;

        public Config(String color, float alpha){
            this.color = color;
            this.alpha = alpha;
        }
    }
}
