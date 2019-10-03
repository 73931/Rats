package com.github.alexthe666.rats.server.entity.ai;

import com.github.alexthe666.rats.server.blocks.BlockRatTube;
import com.github.alexthe666.rats.server.entity.EntityRat;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RatPathNavigate extends PathNavigateGround {

    public BlockPos targetPosition;

    public RatPathNavigate(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);
    }


    protected PathFinder getPathFinder() {
        this.nodeProcessor = new RatWalkNodeProcessor();
        this.nodeProcessor.setCanEnterDoors(true);
        this.nodeProcessor.setCanSwim(true);
        return new RatPathFinder(this.nodeProcessor, (EntityRat) entity);
    }

    public Path getPathToPos(BlockPos pos) {
        this.targetPosition = pos;
        if (entity.world.getBlockState(pos) instanceof BlockRatTube) {
            IBlockState state = entity.world.getBlockState(pos);
            for (int i = 0; i < EnumFacing.values().length; i++) {
                PropertyBool bool = BlockRatTube.ALL_OPEN_PROPS[i];
                if (state.getValue(bool) && entity.getHorizontalFacing().getOpposite() != EnumFacing.values()[i]) {
                    return super.getPathToPos(pos.offset(EnumFacing.values()[i]));
                }
            }
        }
        return super.getPathToPos(pos);
    }

    public Path getPathToEntityLiving(Entity entityIn) {
        this.targetPosition = new BlockPos(entityIn);
        return super.getPathToEntityLiving(entityIn);
    }

    public boolean tryMoveToEntityLiving(Entity entityIn, double speedIn) {
        Path path = this.getPathToEntityLiving(entityIn);

        if (path != null) {
            return this.setPath(path, speedIn);
        } else {
            this.targetPosition = new BlockPos(entityIn);
            this.speed = speedIn;
            return true;
        }
    }

    public void clearPath() {
        super.clearPath();
    }

    public void onUpdateNavigation() {
        ++this.totalTicks;
        ((EntityRat) this.entity).setTubeTarget(this.targetPosition);

        if (this.tryUpdatePath) {
            this.updatePath();
        }
        if (!this.noPath()) {
            if (this.canNavigate()) {
                this.pathFollow();
            } else if (this.currentPath != null && this.currentPath.getCurrentPathIndex() < this.currentPath.getCurrentPathLength()) {
                Vec3d vec3d = this.getEntityPosition();
                Vec3d vec3d1 = this.currentPath.getVectorFromIndex(this.entity, this.currentPath.getCurrentPathIndex());

                if (vec3d.y > vec3d1.y && !this.entity.onGround && MathHelper.floor(vec3d.x) == MathHelper.floor(vec3d1.x) && MathHelper.floor(vec3d.z) == MathHelper.floor(vec3d1.z)) {
                    this.currentPath.setCurrentPathIndex(this.currentPath.getCurrentPathIndex() + 1);
                }
            }
            this.world.profiler.endSection();
            if (!this.noPath()) {
                Vec3d vec3d2 = this.currentPath.getPosition(this.entity);
                this.entity.getMoveHelper().setMoveTo(vec3d2.x, vec3d2.y, vec3d2.z, this.speed);

            }
        } else if (targetPosition != null) {
            double d0 = 1;
            if (this.entity.getDistanceSqToCenter(this.targetPosition) >= d0 && (this.entity.posY <= (double) this.targetPosition.getY() || this.entity.getDistanceSqToCenter(new BlockPos(this.targetPosition.getX(), MathHelper.floor(this.entity.posY), this.targetPosition.getZ())) >= d0)) {
                this.entity.getMoveHelper().setMoveTo((double) this.targetPosition.getX(), (double) this.targetPosition.getY(), (double) this.targetPosition.getZ(), this.speed);
            } else {
                this.targetPosition = null;
            }
        }
    }

    public boolean canEntityStandOnPos(BlockPos pos) {
        if (this.world.getBlockState(pos).getBlock() instanceof BlockRatTube) {
            IBlockState state = this.world.getBlockState(pos);
            return state.getBlock().getMetaFromState(state) > 0;
        }
        return this.world.getBlockState(pos.down()).isFullBlock();
    }
}
