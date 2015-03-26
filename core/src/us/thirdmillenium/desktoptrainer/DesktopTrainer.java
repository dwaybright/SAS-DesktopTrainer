package us.thirdmillenium.desktoptrainer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import us.thirdmillenium.desktoptrainer.environment.Environment;

import java.util.Random;


public class DesktopTrainer extends ApplicationAdapter {
    // Environment
    private Environment MyEnvironment;

    @Override
    public void create () {
        Random random = new Random();

        this.MyEnvironment = new Environment(TrainingParams.PathToBaseNN, random, 5);
    }

    @Override
    public void render () {
        this.MyEnvironment.render(Gdx.graphics.getDeltaTime());
    }
}
