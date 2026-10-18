package fr.leslimbes.client.render.particle;

import fr.leslimbes.registry.LimbesParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

/** Particules originales du mod : étoile royale, éclat de nacre, lueur douce, brume. */
public final class LimbesParticleFactories {
	public static void init() {
		ParticleFactoryRegistry.getInstance().register(LimbesParticles.ETOILE_ROYALE, EtoileParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(LimbesParticles.ECLAT_DE_NACRE, EclatParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(LimbesParticles.LUEUR, LueurParticle.Factory::new);
		ParticleFactoryRegistry.getInstance().register(LimbesParticles.BRUME, BrumeParticle.Factory::new);
	}

	/** Étoile dorée scintillante, ralentit et s'éteint. */
	public static class EtoileParticle extends SpriteBillboardParticle {
		private final SpriteProvider sprites;

		EtoileParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, SpriteProvider sprites) {
			super(world, x, y, z, vx, vy, vz);
			this.sprites = sprites;
			this.velocityX = vx;
			this.velocityY = vy;
			this.velocityZ = vz;
			this.scale = 0.08F + random.nextFloat() * 0.06F;
			this.maxAge = 20 + random.nextInt(20);
			this.gravityStrength = 0.0F;
			this.collidesWithWorld = false;
			this.setSprite(sprites);
		}

		@Override
		public void tick() {
			super.tick();
			this.velocityX *= 0.92;
			this.velocityY *= 0.92;
			this.velocityZ *= 0.92;
			this.alpha = 1.0F - (float) this.age / this.maxAge;
			this.scale *= 0.98F;
		}

		@Override
		public int getBrightness(float tint) {
			return 0xF000F0;
		}

		@Override
		public ParticleTextureSheet getType() {
			return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
		}

		public static class Factory implements ParticleFactory<SimpleParticleType> {
			private final SpriteProvider sprites;

			public Factory(SpriteProvider sprites) {
				this.sprites = sprites;
			}

			@Override
			public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
				return new EtoileParticle(world, x, y, z, vx, vy, vz, sprites);
			}
		}
	}

	/** Éclat de nacre : petit fragment irisé qui tombe lentement. */
	public static class EclatParticle extends SpriteBillboardParticle {
		EclatParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, SpriteProvider sprites) {
			super(world, x, y, z, vx, vy, vz);
			this.velocityX = vx + (random.nextDouble() - 0.5) * 0.04;
			this.velocityY = vy + random.nextDouble() * 0.06;
			this.velocityZ = vz + (random.nextDouble() - 0.5) * 0.04;
			this.scale = 0.06F + random.nextFloat() * 0.04F;
			this.maxAge = 30 + random.nextInt(20);
			this.gravityStrength = 0.15F;
			this.setSprite(sprites);
		}

		@Override
		public void tick() {
			super.tick();
			this.alpha = Math.max(0.0F, 1.0F - (float) this.age / this.maxAge);
		}

		@Override
		public int getBrightness(float tint) {
			return 0xF000F0;
		}

		@Override
		public ParticleTextureSheet getType() {
			return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
		}

		public static class Factory implements ParticleFactory<SimpleParticleType> {
			private final SpriteProvider sprites;

			public Factory(SpriteProvider sprites) {
				this.sprites = sprites;
			}

			@Override
			public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
				return new EclatParticle(world, x, y, z, vx, vy, vz, sprites);
			}
		}
	}

	/** Lueur douce bleutée qui s'élève lentement. */
	public static class LueurParticle extends SpriteBillboardParticle {
		LueurParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, SpriteProvider sprites) {
			super(world, x, y, z, vx, vy, vz);
			this.velocityX = vx;
			this.velocityY = vy + 0.01;
			this.velocityZ = vz;
			this.scale = 0.1F + random.nextFloat() * 0.08F;
			this.maxAge = 40 + random.nextInt(30);
			this.gravityStrength = 0.0F;
			this.collidesWithWorld = false;
			this.setSprite(sprites);
		}

		@Override
		public void tick() {
			super.tick();
			float t = (float) this.age / this.maxAge;
			this.alpha = t < 0.2F ? t / 0.2F : 1.0F - (t - 0.2F) / 0.8F;
		}

		@Override
		public int getBrightness(float tint) {
			return 0xF000F0;
		}

		@Override
		public ParticleTextureSheet getType() {
			return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
		}

		public static class Factory implements ParticleFactory<SimpleParticleType> {
			private final SpriteProvider sprites;

			public Factory(SpriteProvider sprites) {
				this.sprites = sprites;
			}

			@Override
			public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
				return new LueurParticle(world, x, y, z, vx, vy, vz, sprites);
			}
		}
	}

	/** Brume : grande nappe translucide qui se dissipe. */
	public static class BrumeParticle extends SpriteBillboardParticle {
		BrumeParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, SpriteProvider sprites) {
			super(world, x, y, z, vx, vy, vz);
			this.velocityX = vx * 0.3;
			this.velocityY = vy * 0.3 + 0.004;
			this.velocityZ = vz * 0.3;
			this.scale = 0.6F + random.nextFloat() * 0.5F;
			this.maxAge = 50 + random.nextInt(30);
			this.gravityStrength = 0.0F;
			this.collidesWithWorld = false;
			this.setSprite(sprites);
		}

		@Override
		public void tick() {
			super.tick();
			this.alpha = 0.6F * Math.max(0.0F, 1.0F - (float) this.age / this.maxAge);
			this.scale += 0.01F;
		}

		@Override
		public ParticleTextureSheet getType() {
			return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
		}

		public static class Factory implements ParticleFactory<SimpleParticleType> {
			private final SpriteProvider sprites;

			public Factory(SpriteProvider sprites) {
				this.sprites = sprites;
			}

			@Override
			public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
				return new BrumeParticle(world, x, y, z, vx, vy, vz, sprites);
			}
		}
	}

	private LimbesParticleFactories() {
	}
}
