package ru.geekbrains.carsimulator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

public class MyGdxGame extends ApplicationAdapter {
    SpriteBatch batch;
    ShapeRenderer render;
    Texture img;
    TextureRegion carSprite;
    Polygon carPolygon;

    @Override
    public void create() {
        batch = new SpriteBatch();
        render = new ShapeRenderer();
        render.setColor(Color.BLACK);
        img = new Texture("cars-spritesheet.png");
        TextureRegion[][] textureRegions = splitRegion(img, 1, 5);
        carSprite = textureRegions[0][0];
        carPolygon = new Polygon(new float[]{0, 0,
                carSprite.getRegionWidth(), 0,
                carSprite.getRegionWidth(), carSprite.getRegionHeight(),
                0, carSprite.getRegionHeight()});
        carPolygon.setOrigin(carSprite.getRegionWidth() / 2f, carSprite.getRegionHeight() / 2f);
    }

    private TextureRegion[][] splitRegion(Texture texture, int rows, int cols) {
        return TextureRegion.split(texture,
                (texture.getWidth()) / cols,
                (texture.getHeight()) / rows);
    }

    Vector2 accDir = new Vector2(0, 1);
    Vector2 velDir = accDir.cpy();
    Vector2 pos = new Vector2(0, 0);
    Vector2 vel = new Vector2(0, 0);
    Vector2 accel = new Vector2(0, 0);
    boolean reverse = false;

    final float ACC_VAL = 2000f;

    boolean checkBorders() {
        boolean result = false;
        Vector2 p1 = new Vector2(0, 0);
        Vector2 p2 = new Vector2(0, Gdx.graphics.getHeight());
        Vector2 p3 = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Vector2 p4 = new Vector2(Gdx.graphics.getWidth(), 0);

        if (Intersector.intersectSegmentPolygon(p1, p2, carPolygon) && vel.x < 0) {
            result = true;
        } else if (Intersector.intersectSegmentPolygon(p2, p3, carPolygon) && vel.y > 0) {
            result = true;
        } else if (Intersector.intersectSegmentPolygon(p3, p4, carPolygon) && vel.x > 0) {
            result = true;
        } else if (Intersector.intersectSegmentPolygon(p1, p4, carPolygon) && vel.y < 0) {
            result = true;
        }
        if (result) {
            vel = new Vector2(0, 0);
            accel = new Vector2(0, 0);
        }
        return result;
    }

    Vector2 fromTwo(Vector2 p1, Vector2 p2) {
        return new Vector2(p2.x - p1.x, p2.y - p1.y);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear screen

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            accDir = new Vector2(0, 1);
            velDir = accDir.cpy();
            pos = new Vector2(0, 0);
            vel = new Vector2(0, 0);
            accel = new Vector2(0, 0);
            reverse = false;
        }

        float deltaTime = Gdx.graphics.getDeltaTime();

        if (vel.len() > 0.1) {
            velDir = vel.cpy().nor();
        }

        accDir = velDir.cpy();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            accDir = accDir.rotate(50);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            accDir = accDir.rotate(-50);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            if (reverse) {
                if (velDir.isCollinear(accel, 1f)) {
                    accel.set(accDir.x * ACC_VAL, accDir.y * ACC_VAL);
                    reverse = false;
                } else {
                    accel.set(-accDir.x * ACC_VAL, -accDir.y * ACC_VAL);
                }
            } else {
                accel.set(accDir.x * ACC_VAL, accDir.y * ACC_VAL);
            }
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (reverse) {
                accel.set(accDir.x * ACC_VAL, accDir.y * ACC_VAL);
            } else {
                if (velDir.isCollinear(accel, 1f)) {
                    accel.set(accDir.x * ACC_VAL, accDir.y * ACC_VAL);
                    reverse = true;
                } else {
                    accel.set(-accDir.x * ACC_VAL, -accDir.y * ACC_VAL);
                }
            }
        } else {
            accel.setZero();
        }

        if (vel.len() > 0.1f) {
            accel.sub(velDir.cpy().scl(800f));
        } else {
            vel.setZero();
        }

        vel.mulAdd(accel, deltaTime * deltaTime);
        checkBorders();
        pos.mulAdd(vel, deltaTime);

        carPolygon.setPosition(pos.x, pos.y);
        carPolygon.setRotation(velDir.angle() - 90 + (reverse ? 180 : 0));

        batch.begin();
        batch.draw(carSprite, pos.x, pos.y, carSprite.getRegionWidth() / 2f, carSprite.getRegionHeight() / 2f,
                carSprite.getRegionWidth(), carSprite.getRegionHeight(),
                1f, 1f,
                velDir.angle() - 90 + (reverse ? 180 : 0));
        batch.end();

        render.begin(ShapeRenderer.ShapeType.Line);
        render.polygon(carPolygon.getTransformedVertices());
        render.line(pos.x + carSprite.getRegionWidth()/2f, pos.y + carSprite.getRegionHeight()/2f,
                pos.x + carSprite.getRegionWidth()/2f + accel.x / 10, pos.y + carSprite.getRegionHeight()/2f + accel.y / 10 );

        render.line(pos.x + carSprite.getRegionWidth()/2f, pos.y + carSprite.getRegionHeight()/2f,
                pos.x + carSprite.getRegionWidth()/2f + vel.x, pos.y + carSprite.getRegionHeight()/2f + vel.y );
        render.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }
}
