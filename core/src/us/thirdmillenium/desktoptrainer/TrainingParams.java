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

import com.badlogic.gdx.math.Vector2;


/**
 * Created by daniel on 3/26/2015.
 */
public interface TrainingParams {

    /*
	 * Output Path and NN Structure File Path
	 */

    /** The base folder to store all GA run information in.
     *  These files will be generated for you. */
    static final String GA_OutputPath   = "desktop/docs/GARunOutput/Run/";

    /** Path to the base NN to be used by GA. */
    static final String PathToBaseNN    = "desktop/docs/BaseANNs/FirstTest.nnet";

    /** Path to the Training CSVs */
    static final String PathToCSV       = "desktop/docs/TrainingCSVs/trainingCSV2.csv";


	/*
	 * GA Parameters
	 */

    /** The number of Genomes to have each generation. */
    static final int NumGenomes = 500;

    /** The number of Generations to run the GA for. */
    static final int NumGenerations = 5;

    /** How many Elite Genomes to keep? **/
    static final int NumEliteGenomes = (int)(NumGenomes * 0.20);

    /** Perform Crossovers. */
    static final boolean PerformCrossovers = true;

    /** Percent of network weights to Crossover */
    static final double CrossoverPercent = 0.30;

    /** Perform Mutations. */
    static final boolean PerformMutations = true;

    /** What percent (+/-) of the weights should be mutated. */
    static final double PercentMutations = 0.70;

    /** Maximum percent change performed by mutation. */
    static final double MaxPercentMutationChange = 0.15;

    /** Number of Threads used by FitnessScorer */
    static final int NumThreads = 10;


	/*
	 * Game Simulation Parameters
	 */

    /** All TileMap levels reside in this directory */
    static final String TileMapsPath            = "core/assets/";

    /** The path to the level to train NN on */
    static final String TileMapLevelPath        = "core/assets/MyCrappyMap2.tmx";

    /** Texture (*.png) for Training Agent. */
    static final String TrainingAgentLivePNG    = "core/assets/goodGuyDotArrow.png";

    /** Texture (*.png) for Shooting Agent. */
    static final String ShootingAgentLivePNG    = "core/assets/badGuyDotArrow.png";

    /** Texture for a deceased dot. */
    static final String DeadAgentPNG            = "core/assets/deadDot.png";


    /** Pixel Size for Tiles */
    static final int MapTileSize = 32;

    /** Pixel Size for Agents */
    static final int AgentTileSize = 20;

    /** Cells in Y direction */
    static final int NumCellsY = 38;

    /** Cells in X direction */
    static final int NumCellsX = 25;

    /** The number of simulation steps to take. Assume ~30 FPS, then 1800 is ~1 minute. */
    static final int SimulationTimeSteps = 2700;

    /** Assumed FPS rate. */
    static final double FramesPerSecond = 30;

    /** Bullet Velocity (pixels/frame). */
    static final float BulletVelocity = 20;

    /** Bullet Path vertices at Origin (0,0) */
    static final float[] BulletPathOriginVertices = {
            -2.5f, 0,
            2.5f, 0,
            2.5f, BulletVelocity,
            -2.5f, BulletVelocity
    };

    /** Agent Rotation Modification Per Time Step */
    static final float[] AgentRotationModArray = { -1, -0.666667f, -0.333333f, 0, 0.333333f, 0.666667f, 1 };

    ///** Agent Fire Rotation Modification Per Time Step */
    //static final double[] AgentFireRotationModArray = { -1, -, -5, 0, 5, 15, 1 };

    /** Agent Velocity Modification Per Time Step */
    static final float[] AgentVelocityModArray = { -0.8f, -0.5f, -0.1f, 0, 0.2f, 0.6f, 1 };

    /** Agent Velocity (pixels/frame). */
    static final float AgentMaxMovement = 5f;

    /** How Far an Agent can Rotate in a frame. */
    static final float AgentMaxTurnAngle = 15f;

    /** Agent Fire Rate (fire/sec) */
    static final float AgentFireRate = 0.5f;

    /** Shooting Agent hit points */
    static final short ShootingAgentHitPoints = 2;

    /** Shooting Agent Accuracy */
    static final float ShootingAgentFireAccuracy = 30f;

    /** Unit vector for Rotation calculations */
    static final Vector2 unitVector = new Vector2(0,1);


	/*
	 * 	Movement Scoring
	 */

    /** Score for moving to new Tile */
    static final int ScoreMoveNewTile = 0;

    /** Score for moving to new Tile on Path */
    static final int ScoreMoveToPrefPathTile = 10;
}
