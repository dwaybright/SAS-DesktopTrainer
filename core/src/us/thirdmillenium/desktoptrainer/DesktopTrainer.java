package us.thirdmillenium.desktoptrainer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import us.thirdmillenium.desktoptrainer.environment.SinglePlayEnvironment;

import java.util.Random;


public class DesktopTrainer extends ApplicationAdapter {
    // Test Map Index (1 - 5)
    private int TestMapIndex = 3;

    // Environment
    private SinglePlayEnvironment MyEnvironment;


    @Override
    public void create () {
        Random random = new Random();

        this.MyEnvironment = new SinglePlayEnvironment(TrainingParams.PathToBaseNN, random, TestMapIndex);
    }

    @Override
    public void render () {
        this.MyEnvironment.simulate(Gdx.graphics.getDeltaTime());
    }
}
