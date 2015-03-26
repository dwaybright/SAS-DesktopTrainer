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

package us.thirdmillenium.desktoptrainer.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import us.thirdmillenium.desktoptrainer.DesktopTrainer;
import us.thirdmillenium.desktoptrainer.geneticalgorithm.GeneticAlgorithm;


public class DesktopLauncher {

	public static void main (String[] arg) {

        boolean DISPLAY = true;
        boolean GA = false;

        LwjglApplicationConfiguration config;


        if( DISPLAY ) {
            config = new LwjglApplicationConfiguration();

            config.title = "Desktop Analyzer";

            config.width  = (25 * 32) / 2; // 400
            config.height = (38 * 32) / 2; // 608

            //config.x = -1;
            //config.y = -1;

            //config.fullscreen = true;

            new LwjglApplication(new DesktopTrainer(), config);
        }

        if( GA ) {
            // Generate a Genetic Algorithm object
            config = new LwjglApplicationConfiguration();

            config.title = "Desktop Trainer";

            config.width = 200;
            config.height = 200;

            config.x = -1;
            config.y = -1;

            //config.fullscreen = true;

            new LwjglApplication(new GeneticAlgorithm(), config);
        }
	}
}
