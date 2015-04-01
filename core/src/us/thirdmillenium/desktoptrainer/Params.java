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


/**
 * Created by daniel on 3/26/2015.
 */
public interface Params {

    /*
     *      Random Math Constants
     */

    static final float SQRT2 = 1.4142135f;


	/*
	 *      GA Parameters
	 */

    /** The base folder to store all GA run information in.
     *  These files will be generated for you. */
    static final String GA_OutputPath   = "core/docs/GARunOutput/Run/";

    /** Path to the base NN to be used by GA. */
    static final String PathToBaseNN    = "core/docs/BaseANNs/MLP3_TanH_Level2.nnet";

    /** Path to the Training CSVs */
    static final String PathToCSV       = "core/docs/TrainingCSVs/trainingCSV3.csv";

    /** The number of Genomes to have each generation. */
    static final int NumGenomes = 1000;

    /** The number of Generations to run the GA for. */
    static final int NumGenerations = 100;

    /** How many Elite Genomes to keep? **/
    static final int NumEliteGenomes = (int)(NumGenomes * 0.35);

    /** Perform Crossovers. */
    static final boolean PerformCrossovers = true;

    /** Percent of network weights to Crossover */
    static final double CrossoverPercent = 0.35;

    /** Perform Mutations. */
    static final boolean PerformMutations = true;

    /** What percent (+/-) of the weights should be mutated. */
    static final double PercentMutations = 0.35;

    /** Maximum percent change performed by mutation. */
    static final double MaxPercentMutationChange = 0.15;

    /** Number of Threads used by FitnessScorer */
    static final int NumThreads = 10;


    /*
	 * 	Movement Scoring
	 */

    /** Score for moving to new Tile */
    static final int ScoreMoveNewTile = 0;

    /** Score for moving to new Tile on Path */
    static final int ScoreMoveToPrefPathTile = 10;


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

    /** The squared radius for an Agent - Bounding Circle */
    static final float AgentRadiusSquared = (AgentTileSize * AgentTileSize) / 4f;

    /** Pixel radius for Agent (Bounding Circle) */
    static final int AgentCircleRadius = 10;

    /** Cells in Y direction */
    static final int NumCellsY = 38;

    /** Cells in X direction */
    static final int NumCellsX = 25;


    /** The number of simulation steps to take. Assume ~30 FPS, then 1800 is ~1 minute. */
    static final int SimulationTimeSteps = 1000;

    /** Assumed FPS rate. */
    static final double FramesPerSecond = 30;


    /** Bullet Velocity (pixels/frame). */
    static final float BulletVelocity = 5;

    /** Bullet Path vertices at Origin (0,0) */
    static final float[] BulletPathOriginVertices = {
            -2.5f, 0,
             2.5f, 0,
             2.5f, BulletVelocity,
            -2.5f, BulletVelocity
    };

    /** Agent Rotation Modification Per Time Step */
    static final float[] AgentRotationModArray = { -1f, -0.666667f, -0.333333f, 0.001f, 0.333333f, 0.666667f, 1f };

    /** Agent Velocity Modification Per Time Step */
    static final float[] AgentVelocityModArray = { -0.8f, -0.5f, -0.1f, 0.1f, 0.2f, 0.6f, 1f };


    /** Agent Velocity (pixels/frame). */
    static final float AgentMaxMovement = 2f;

    /** How Far an Agent can Rotate in a frame. */
    static final float AgentMaxTurnAngle = 15f;

    /** Agent Fire Rate (fire/sec) */
    static final float AgentFireRate = 0.5f;

    static final int AgentFireRateCooldown = (int)FramesPerSecond / 2;


    /** Shooting Agent hit points */
    static final short EnemyAgentHitPoints = 2;

    /** Shooting Agent hit points */
    static final short TeamAgentHitPoints = 200;

    /** Shooting Agent Accuracy */
    static final float ShootingAgentFireAccuracy = 30f;


    /*
     *      Cone Agent Vision Settings
     */

    static final float ConeVisNorm = 4;

    static final float ConeVisEmpty = 0;

    static final float ConeVisWall = 1 / ConeVisNorm;

    static final float ConeVisTeam = 2 / ConeVisNorm;

    static final float ConeVisPath = 3 / ConeVisNorm;

    static final float ConeVisEnemy = 4 / ConeVisNorm;

}
