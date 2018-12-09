package ru.geekbrains.carsimulator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class MyGdxGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture img;
    TextureRegion carSprite;

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture("cars-spritesheet.png");
        TextureRegion[][] textureRegions = splitRegion(img, 1, 5);
        carSprite = textureRegions[0][0];
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

    final float ACC_VAL = 80f;

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear screen

        float deltaTime = Gdx.graphics.getDeltaTime();

        if (vel.len() > 0.1) {
            velDir = vel.cpy().nor();
        }

        accDir = velDir.cpy();
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            accDir = accDir.rotate(90);
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            accDir = accDir.rotate(-90);
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

//        if (vel.len() > 0.1) {
//            accel.sub(Math.signum(velDir.x) * 3f, Math.signum(velDir.y) * 3f);
//        }

        vel.mulAdd(accel, deltaTime);
        pos.mulAdd(vel, deltaTime);

        batch.begin();
        batch.draw(carSprite, pos.x, pos.y, carSprite.getRegionWidth() / 2f, carSprite.getRegionHeight() / 2f,
                carSprite.getRegionWidth(), carSprite.getRegionHeight(),
                0.5f, 0.5f,
                velDir.angle() - 90 + (reverse ? 180 : 0));
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
    }
}
