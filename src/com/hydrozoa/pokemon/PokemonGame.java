package com.hydrozoa.pokemon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.hydrozoa.pokemon.battle.animation.AnimatedBattleSprite;
import com.hydrozoa.pokemon.battle.animation.BattleAnimation;
import com.hydrozoa.pokemon.battle.animation.BattleAnimationAccessor;
import com.hydrozoa.pokemon.battle.animation.BattleSprite;
import com.hydrozoa.pokemon.battle.animation.BattleSpriteAccessor;
import com.hydrozoa.pokemon.battle.moves.MoveDatabase;
import com.hydrozoa.pokemon.screen.AbstractScreen;
import com.hydrozoa.pokemon.screen.BattleScreen;
import com.hydrozoa.pokemon.screen.GameScreen;
import com.hydrozoa.pokemon.screen.TransitionScreen;
import com.hydrozoa.pokemon.screen.transition.Action;
import com.hydrozoa.pokemon.screen.transition.BattleBlinkTransition;
import com.hydrozoa.pokemon.screen.transition.BattleBlinkTransitionAccessor;
import com.hydrozoa.pokemon.screen.transition.Transition;
import com.hydrozoa.pokemon.util.SkinGenerator;

import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

/**
 * Topmost class of the game. Logic is delegated to Screens from here.
 * 
 * @author hydrozoa
 */
public class PokemonGame extends Game {
	
	private GameScreen gameScreen;
	private BattleScreen battleScreen;
	private TransitionScreen transitionScreen;
	
	private MoveDatabase moveDatabase;
	
	private AssetManager assetManager;
	
	private Skin skin;
	
	private TweenManager tweenManager;
	
	private ShaderProgram overlayShader;
	private ShaderProgram transitionShader;

	@Override
	public void create() {
		/*
		 * LOADING SHADERS
		 */
		ShaderProgram.pedantic = false;
		overlayShader = new ShaderProgram(
				Gdx.files.internal("res/shaders/overlay/vertexshader.txt"), 
				Gdx.files.internal("res/shaders/overlay/fragmentshader.txt"));
		if (!overlayShader.isCompiled()) {
			System.out.println(overlayShader.getLog());
		}
		transitionShader = new ShaderProgram(
				Gdx.files.internal("res/shaders/transition/vertexshader.txt"), 
				Gdx.files.internal("res/shaders/transition/fragmentshader.txt"));
		if (!transitionShader.isCompiled()) {
			System.out.println(transitionShader.getLog());
		}
		
		/*
		 * SETTING UP TWEENING
		 */
		tweenManager = new TweenManager();
		Tween.registerAccessor(BattleAnimation.class, new BattleAnimationAccessor());
		Tween.registerAccessor(BattleSprite.class, new BattleSpriteAccessor());
		Tween.registerAccessor(AnimatedBattleSprite.class, new BattleSpriteAccessor());
		Tween.registerAccessor(BattleBlinkTransition.class, new BattleBlinkTransitionAccessor());
		
		/*
		 * LOADING ASSETS
		 */
		assetManager = new AssetManager();
		assetManager.load("res/graphics_packed/tiles/tilepack.atlas", TextureAtlas.class);
		assetManager.load("res/graphics_packed/ui/uipack.atlas", TextureAtlas.class);
		assetManager.load("res/graphics_packed/battle/battlepack.atlas", TextureAtlas.class);
		assetManager.load("res/graphics/pokemon/bulbasaur.png", Texture.class);
		assetManager.load("res/graphics/pokemon/slowpoke.png", Texture.class);
		
		for (int i = 0; i < 32; i++) {
			assetManager.load("res/graphics/statuseffect/attack_"+i+".png", Texture.class);
		}
		assetManager.load("res/graphics/statuseffect/white.png", Texture.class);
		
		for (int i = 0; i < 13; i++) {
			assetManager.load("res/graphics/transitions/transition_"+i+".png", Texture.class);
		}
		assetManager.load("res/font/small_letters_font.fnt", BitmapFont.class);
		
		assetManager.finishLoading();
		
		skin = SkinGenerator.generateSkin(assetManager);
		
		moveDatabase = new MoveDatabase();
		
		gameScreen = new GameScreen(this);
		battleScreen = new BattleScreen(this);
		transitionScreen = new TransitionScreen(this);
		
		this.setScreen(gameScreen);
	}
	
	@Override
	public void render() {
		//System.out.println(Gdx.graphics.getFramesPerSecond());
		
		/* UPDATE */
		tweenManager.update(Gdx.graphics.getDeltaTime());
		if (getScreen() instanceof AbstractScreen) {
			((AbstractScreen)getScreen()).update(Gdx.graphics.getDeltaTime());
		}
		
		/* RENDER */
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		getScreen().render(Gdx.graphics.getDeltaTime());
	}
	
	public AssetManager getAssetManager() {
		return assetManager;
	}
	
	public Skin getSkin() {
		return skin;
	}
	
	public TweenManager getTweenManager() {
		return tweenManager;
	}
	
	public GameScreen getGameScreen() {
		return gameScreen;
	}
	
	public BattleScreen getBattleScreen() {
		return battleScreen;
	}
	
	public void startTransition(AbstractScreen from, AbstractScreen to, Transition out, Transition in, Action action) {
		transitionScreen.startTransition(from, to, out, in, action);
	}
	
	public ShaderProgram getOverlayShader() {
		return overlayShader;
	}
	
	public ShaderProgram getTransitionShader() {
		return transitionShader;
	}
	
	public MoveDatabase getMoveDatabase() {
		return moveDatabase;
	}
}
