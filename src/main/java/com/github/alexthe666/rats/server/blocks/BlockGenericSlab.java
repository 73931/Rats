package com.github.alexthe666.rats.server.blocks;

import com.github.alexthe666.rats.RatsMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class BlockGenericSlab extends SlabBlock {

    private final Block baseBlock;

    public BlockGenericSlab(String name, float hardness, float resistance, SoundType soundType, Material material, Block baseBlock) {
        super(material);
        BlockState BlockState = this.blockState.getBaseState();
        this.baseBlock = baseBlock;
        this.setLightOpacity(0);
        this.useNeighborBrightness = true;
        setHardness(hardness);
        setResistance(resistance);
        setSoundType(soundType);
        if (this.isDouble()) {
            setTranslationKey("rats." + name + "_double");
            this.setRegistryName(name + "_double");
        } else {
            BlockState = BlockState.with(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
            setTranslationKey("rats." + name);
            this.setRegistryName(name);
            this.setCreativeTab(RatsMod.TAB);
        }
    }

    @SideOnly(Side.CLIENT)
    protected static boolean isHalfSlab(BlockState state) {
        return state.getBlock() instanceof BlockGenericSlab && !((BlockGenericSlab) state.getBlock()).isDouble();
    }

    @Override
    @Nullable
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return getItem();
    }

    public abstract BlockItem getItemBlock();

    public abstract Item getItem();

    @SuppressWarnings("deprecation")
    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, BlockState state) {
        return new ItemStack(getItem());
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState getStateFromMeta(int meta) {
        BlockState BlockState = this.getDefaultState();
        if (!this.isDouble()) {
            return BlockState.with(HALF, meta == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
        } else {
            return BlockState;
        }
    }

    @Override
    public int getMetaFromState(BlockState state) {
        int i = 0;

        if (!this.isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
            i = 1;
        }

        return i;

    }

    @Override
    protected BlockStateContainer createBlockState() {
        return this.isDouble() ? super.createBlockState() : new BlockStateContainer(this, HALF);
    }

    @Override
    public String getTranslationKey() {
        return super.getTranslationKey();
    }

    public String getTranslationKey(int meta) {
        return super.getTranslationKey();
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return null;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return Variant.DEFAULT;
    }

    public enum Variant implements IStringSerializable {
        DEFAULT;

        @Override
        public String getName() {
            return "default";
        }
    }

    public abstract static class Double extends BlockGenericSlab {
        public Double(String name, float hardness, float resistance, SoundType soundType, Material material, Block baseBlock) {
            super(name, hardness, resistance, soundType, material, baseBlock);
        }

        @Override
        public boolean isDouble() {
            return true;
        }
    }

    public abstract static class Half extends BlockGenericSlab {
        public Half(String name, float hardness, float resistance, SoundType soundType, Material material, Block baseBlock) {
            super(name, hardness, resistance, soundType, material, baseBlock);
        }

        @Override
        public boolean isDouble() {
            return false;
        }

    }

    class ItemBlockGenericSlab extends ItemBlock {
        private final Block singleSlab;
        private final Block doubleSlab;

        public ItemBlockGenericSlab(Block block, Block singleSlab, Block doubleSlab) {
            super(block);
            this.singleSlab = singleSlab;
            this.doubleSlab = doubleSlab;
            this.setMaxDamage(0);
            this.setHasSubtypes(true);
        }

        @Override
        public int getMetadata(int damage) {
            return damage;
        }

        @Override
        public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
            ItemStack stack = playerIn.getHeldItem(hand);
            if (stack.getItem() == Item.getItemFromBlock(doubleSlab)) {
                return EnumActionResult.SUCCESS;
            }
            if (stack.getCount() != 0 && playerIn.canPlayerEdit(pos.offset(facing), facing, stack)) {
                Comparable<?> comparable = Variant.DEFAULT;
                BlockState BlockState = worldIn.getBlockState(pos);
                if (BlockState.getBlock() == this.singleSlab) {
                    BlockSlab.EnumBlockHalf blockslab$enumblockhalf = BlockState.getValue(BlockSlab.HALF);
                    if ((facing == Direction.UP && blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.BOTTOM || facing == Direction.DOWN && blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.TOP)) {
                        BlockState BlockState1 = this.doubleSlab.getDefaultState();
                        AxisAlignedBB axisalignedbb = BlockState1.getCollisionBoundingBox(worldIn, pos);
                        if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos)) && worldIn.setBlockState(pos, BlockState1, 11)) {
                            SoundType soundtype = this.doubleSlab.getSoundType(BlockState1, worldIn, pos, playerIn);
                            worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                            stack.shrink(1);
                        }
                        return EnumActionResult.SUCCESS;
                    }
                }
                return this.tryPlace(playerIn, stack, worldIn, pos.offset(facing), comparable) ? EnumActionResult.SUCCESS : super.onItemUse(playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
            } else {
                return EnumActionResult.FAIL;
            }
        }

        @Override
        @SideOnly(Side.CLIENT)
        public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, Direction side, EntityPlayer player, ItemStack stack) {
            BlockPos blockpos = pos;
            BlockState BlockState = worldIn.getBlockState(pos);

            if (BlockState.getBlock() == this.singleSlab) {
                boolean flag = BlockState.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;
                if ((side == Direction.UP && !flag || side == Direction.DOWN && flag)) {
                    return true;
                }
            }
            pos = pos.offset(side);
            BlockState BlockState1 = worldIn.getBlockState(pos);
            return BlockState1.getBlock() == this.singleSlab || super.canPlaceBlockOnSide(worldIn, blockpos, side, player, stack);
        }

        private boolean tryPlace(EntityPlayer player, ItemStack stack, World worldIn, BlockPos pos, Object itemSlabType) {
            BlockState BlockState = worldIn.getBlockState(pos);
            if (BlockState.getBlock() == this.singleSlab) {
                BlockState BlockState1 = this.doubleSlab.getDefaultState();
                AxisAlignedBB axisalignedbb = BlockState1.getCollisionBoundingBox(worldIn, pos);
                if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos)) && worldIn.setBlockState(pos, BlockState1, 11)) {
                    SoundType soundtype = this.doubleSlab.getSoundType(BlockState1, worldIn, pos, player);
                    worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    stack.shrink(1);
                }
                return true;
            }

            return false;
        }

        @Override
        public String getTranslationKey(ItemStack stack) {
            return this.singleSlab.getTranslationKey();
        }

    }
}
