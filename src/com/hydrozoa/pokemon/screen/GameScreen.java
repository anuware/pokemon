package com.hydrozoa.pokemon.screen;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.hydrozoa.pokemon.PokemonGame;
import com.hydrozoa.pokemon.controller.ActorMovementController;
import com.hydrozoa.pokemon.controller.DialogueController;
import com.hydrozoa.pokemon.controller.InteractionController;
import com.hydrozoa.pokemon.dialogue.Dialogue;
import com.hydrozoa.pokemon.model.Camera;
import com.hydrozoa.pokemon.model.DIRECTION;
import com.hydrozoa.pokemon.model.actor.PlayerActor;
import com.hydrozoa.pokemon.model.world.Door;
import com.hydrozoa.pokemon.model.world.World;
import com.hydrozoa.pokemon.model.world.WorldObject;
import com.hydrozoa.pokemon.model.world.cutscene.ActorWalkEvent;
import com.hydrozoa.pokemon.model.world.cutscene.CutsceneEvent;
import com.hydrozoa.pokemon.model.world.cutscene.CutsceneEventQueuer;
import com.hydrozoa.pokemon.model.world.cutscene.CutscenePlayer;
import com.hydrozoa.pokemon.model.world.cutscene.DoorEvent;
import com.hydrozoa.pokemon.screen.renderer.EventQueueRenderer;
import com.hydrozoa.pokemon.screen.renderer.WorldRenderer;
import com.hydrozoa.pokemon.screen.transition.Action;
import com.hydrozoa.pokemon.screen.transition.FadeInTransition;
import com.hydrozoa.pokemon.screen.transition.FadeOutTransition;
import com.hydrozoa.pokemon.ui.DialogueBox;
import com.hydrozoa.pokemon.ui.OptionBox;
import com.hydrozoa.pokemon.util.AnimationSet;
import com.hydrozoa.pokemon.util.MapUtil;

/**
 * @author hydrozoa
 */
public class GameScreen extends AbstractScreen implements CutscenePlayer, CutsceneEventQueuer {
	
	private InputMultiplexer multiplexer;
	private DialogueController dialogueController;
	private ActorMovementController playerController;
	private InteractionController interactionController;
	
	private HashMap<String, World> worlds = new HashMap<String, World>();
	private World world;
	private PlayerActor player;
	private Camera camera;
	private Dialogue dialogue;
	private MapUtil mapUtil;
	private Queue<CutsceneEvent> eventQueue = new ArrayDeque<CutsceneEvent>();
	private CutsceneEvent currentEvent;
	
	private SpriteBatch batch;
	
	private Viewport gameViewport;
	
	private WorldRenderer worldRenderer;
	private EventQueueRenderer queueRenderer;
	
	private int uiScale = 2;
	
	private Stage uiStage;
	private Table root;
	private DialogueBox dialogueBox;
	private OptionBox optionsBox;

	public GameScreen(PokemonGame app) {
		super(app);
		gameViewport = new ScreenViewport();
		batch = new SpriteBatch();
		
		TextureAtlas atlas = app.getAssetManager().get("res/graphics_packed/tiles/tilepack.atlas", TextureAtlas.class);
		
		AnimationSet animations = new AnimationSet(
				new Animation(0.3f/2f, atlas.findRegions("brendan_walk_north"), PlayMode.LOOP_PINGPONG),
				new Animation(0.3f/2f, atlas.findRegions("brendan_walk_south"), PlayMode.LOOP_PINGPONG),
				new Animation(0.3f/2f, atlas.findRegions("brendan_walk_east"), PlayMode.LOOP_PINGPONG),
				new Animation(0.3f/2f, atlas.findRegions("brendan_walk_west"), PlayMode.LOOP_PINGPONG),
				atlas.findRegion("brendan_stand_north"),
				atlas.findRegion("brendan_stand_south"),
				atlas.findRegion("brendan_stand_east"),
				atlas.findRegion("brendan_stand_west")
		);
		
		mapUtil = new MapUtil(app.getAssetManager(), this, this, animations);
		worlds.put("test_level", mapUtil.loadWorld1());
		worlds.put("test_indoors", mapUtil.loadWorld2());
		
		world = worlds.get("test_level");
		
		camera = new Camera();
		player = new PlayerActor(world, 4, 4, animations);
		world.addActor(player);
		
		initUI();
		
		multiplexer = new InputMultiplexer();
		
		playerController = new ActorMovementController(player);
		dialogueController = new DialogueController(dialogueBox, optionsBox);
		interactionController = new InteractionController(player, dialogueController);
		multiplexer.addProcessor(0, dialogueController);
		multiplexer.addProcessor(1, playerController);
		multiplexer.addProcessor(2, interactionController);
		
		worldRenderer = new WorldRenderer(getApp().getAssetManager(), world);
		queueRenderer = new EventQueueRenderer(app.getSkin(), eventQueue);
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public void hide() {
		
	}

	@Override
	public void pause() {
		
	}
	
	@Override
	public void update(float delta) {
		if (Gdx.input.isKeyJustPressed(Keys.J)) {
			WorldObject obj = world.getMap().getTile(13, 10).getObject();
			if (obj instanceof Door) {
				Door door = (Door)obj;
//				if (door.getState() == Door.STATE.CLOSED) {
//					door.open();
//				} else if (door.getState() == Door.STATE.OPEN) {
//					door.close();
//				}
				
				queueEvent(new DoorEvent(door, true));
				queueEvent(new DoorEvent(door, false));
			}
		}
		if (Gdx.input.isKeyJustPressed(Keys.K)) {
			queueEvent(new ActorWalkEvent(player, DIRECTION.NORTH));
		}
		
		while (currentEvent == null || currentEvent.isFinished()) { // no active event
			if (eventQueue.peek() == null) { // no event queued up
				currentEvent = null;
				break;
			} else {					// event queued up
				currentEvent = eventQueue.poll();
				currentEvent.begin(this);
			}
		}
		
		if (currentEvent != null) {
			currentEvent.update(delta);
		}
			
		if (currentEvent == null) {
			playerController.update(delta);
		}
		
		dialogueController.update(delta);
		
		if (!dialogueBox.isVisible()) {
			camera.update(player.getWorldX()+0.5f, player.getWorldY()+0.5f);
			world.update(delta);
		}
		uiStage.act(delta);
	}

	@Override
	public void render(float delta) {
		gameViewport.apply();
		batch.begin();
		worldRenderer.render(batch, camera);
		//queueRenderer.render(batch, currentEvent);
		batch.end();
		
		uiStage.draw();
	}

	@Override
	public void resize(int width, int height) {
		batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		uiStage.getViewport().update(width/uiScale, height/uiScale, true);
		gameViewport.update(width, height);
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(multiplexer);
		if (currentEvent != null) {
			currentEvent.screenShow();
		}
	}
	
	private void initUI() {
		uiStage = new Stage(new ScreenViewport());
		uiStage.getViewport().update(Gdx.graphics.getWidth()/uiScale, Gdx.graphics.getHeight()/uiScale, true);
		//uiStage.setDebugAll(true);
		
		root = new Table();
		root.setFillParent(true);
		uiStage.addActor(root);
		
		dialogueBox = new DialogueBox(getApp().getSkin());
		dialogueBox.setVisible(false);
		
		optionsBox = new OptionBox(getApp().getSkin());
		optionsBox.setVisible(false);
		
		Table dialogTable = new Table();
		dialogTable.add(optionsBox)
						.expand()
						.align(Align.right)
						.space(8f)
						.row();
		dialogTable.add(dialogueBox)
						.expand()
						.align(Align.bottom)
						.space(8f)
						.row();
		
		
		root.add(dialogTable).expand().align(Align.bottom);
	}
	
	public void changeWorld(World newWorld, int x, int y, DIRECTION face) {
		player.changeWorld(newWorld, x, y);
		this.world = newWorld;
		player.refaceWithoutAnimation(face);
		this.worldRenderer.setWorld(newWorld);
		this.camera.update(player.getWorldX()+0.5f, player.getWorldY()+0.5f);
	}

	@Override
	public void changeLocation(World newWorld, int x, int y, DIRECTION facing, Color color) {
		getApp().startTransition(
				this, 
				this, 
				new FadeOutTransition(0.8f, color, getApp().getTweenManager(), getApp().getAssetManager()), 
				new FadeInTransition(0.8f, color, getApp().getTweenManager(), getApp().getAssetManager()), 
				new Action() {
					@Override
					public void action() {
						changeWorld(newWorld, x, y, facing);
					}
				});
	}

	@Override
	public World getWorld(String worldName) {
		return worlds.get(worldName);
	}

	@Override
	public void queueEvent(CutsceneEvent event) {
		eventQueue.add(event);
	}
}
