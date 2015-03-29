/*
 Copyright (C) 2015 Daniel Waybright, daniel.waybright@gmail.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package us.thirdmillenium.desktoptrainer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;

import us.thirdmillenium.desktoptrainer.environment.Environment;
import us.thirdmillenium.desktoptrainer.environment.SinglePlayEnvironment;

import java.util.Random;


public class DesktopTrainer extends ApplicationAdapter {
    // Test Index
    private int TestIndex = 2;

    // Environment
    private Environment MyEnvironment;

    @Override
    public void create () {
        Random random = new Random();

        this.MyEnvironment = new SinglePlayEnvironment(Params.PathToBaseNN, random, TestIndex);
    }

    @Override
    public void render () {
        this.MyEnvironment.simulate(Gdx.graphics.getDeltaTime());
    }
}
