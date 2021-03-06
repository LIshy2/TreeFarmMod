package net.minecraft.block;

import java.util.Random;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ObserverBlock extends DirectionalBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   public ObserverBlock(Block.Properties properties) {
      super(properties);
      this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH).with(POWERED, Boolean.valueOf(false)));
   }

   protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
      builder.add(FACING, POWERED);
   }

   public BlockState rotate(BlockState state, Rotation rot) {
      return state.with(FACING, rot.rotate(state.get(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirrorIn) {
      return state.rotate(mirrorIn.toRotation(state.get(FACING)));
   }

   public void func_225534_a_(BlockState p_225534_1_, ServerWorld p_225534_2_, BlockPos p_225534_3_, Random p_225534_4_) {
      if (p_225534_1_.get(POWERED)) {
         p_225534_2_.setBlockState(p_225534_3_, p_225534_1_.with(POWERED, Boolean.valueOf(false)), 2);
      } else {
         p_225534_2_.setBlockState(p_225534_3_, p_225534_1_.with(POWERED, Boolean.valueOf(true)), 2);
         p_225534_2_.getPendingBlockTicks().scheduleTick(p_225534_3_, this, 2);
      }

      this.updateNeighborsInFront(p_225534_2_, p_225534_3_, p_225534_1_);
   }

   public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
      if (stateIn.get(FACING) == facing && !stateIn.get(POWERED)) {
         this.startSignal(worldIn, currentPos);
      }

      return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
   }

   private void startSignal(IWorld worldIn, BlockPos pos) {
      if (!worldIn.isRemote() && !worldIn.getPendingBlockTicks().isTickScheduled(pos, this)) {
         worldIn.getPendingBlockTicks().scheduleTick(pos, this, 2);
      }

   }

   protected void updateNeighborsInFront(World worldIn, BlockPos pos, BlockState state) {
      Direction direction = state.get(FACING);
      BlockPos blockpos = pos.offset(direction.getOpposite());
      worldIn.neighborChanged(blockpos, this, pos);
      worldIn.notifyNeighborsOfStateExcept(blockpos, this, direction);
   }

   public boolean canProvidePower(BlockState state) {
      return true;
   }

   public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
      return blockState.getWeakPower(blockAccess, pos, side);
   }

   public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
      return blockState.get(POWERED) && blockState.get(FACING) == side ? 15 : 0;
   }

   public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
      if (state.getBlock() != oldState.getBlock()) {
         if (!worldIn.isRemote() && state.get(POWERED) && !worldIn.getPendingBlockTicks().isTickScheduled(pos, this)) {
            BlockState blockstate = state.with(POWERED, Boolean.valueOf(false));
            worldIn.setBlockState(pos, blockstate, 18);
            this.updateNeighborsInFront(worldIn, pos, blockstate);
         }

      }
   }

   public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock()) {
         if (!worldIn.isRemote && state.get(POWERED) && worldIn.getPendingBlockTicks().isTickScheduled(pos, this)) {
            this.updateNeighborsInFront(worldIn, pos, state.with(POWERED, Boolean.valueOf(false)));
         }

      }
   }

   public BlockState getStateForPlacement(BlockItemUseContext context) {
      return this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite().getOpposite());
   }
}