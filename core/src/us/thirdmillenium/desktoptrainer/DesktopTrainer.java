package us.thirdmillenium.desktoptrainer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import us.thirdmillenium.desktoptrainer.environment.Environment;
import us.thirdmillenium.desktoptrainer.environment.SinglePlayEnvironment;

import java.util.Random;


public class DesktopTrainer extends ApplicationAdapter {
    // Test INdex
    private int TestIndex = 3;

    // Environment
    private Environment MyEnvironment;

    @Override
    public void create () {
        Random random = new Random();

        this.MyEnvironment = new SinglePlayEnvironment(TrainingParams.PathToBaseNN, random, TestIndex);
    }

    @Override
    public void render () {
        this.MyEnvironment.render(Gdx.graphics.getDeltaTime());
    }
}
