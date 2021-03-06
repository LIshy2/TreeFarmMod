package net.minecraft.inventory.container;

import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerContainer extends RecipeBookContainer<CraftingInventory> {
   public static final ResourceLocation field_226615_c_ = new ResourceLocation("textures/atlas/blocks.png");
   public static final ResourceLocation field_226616_d_ = new ResourceLocation("item/empty_armor_slot_helmet");
   public static final ResourceLocation field_226617_e_ = new ResourceLocation("item/empty_armor_slot_chestplate");
   public static final ResourceLocation field_226618_f_ = new ResourceLocation("item/empty_armor_slot_leggings");
   public static final ResourceLocation field_226619_g_ = new ResourceLocation("item/empty_armor_slot_boots");
   public static final ResourceLocation field_226620_h_ = new ResourceLocation("item/empty_armor_slot_shield");
   private static final ResourceLocation[] ARMOR_SLOT_TEXTURES = new ResourceLocation[]{field_226619_g_, field_226618_f_, field_226617_e_, field_226616_d_};
   private static final EquipmentSlotType[] VALID_EQUIPMENT_SLOTS = new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};
   private final CraftingInventory field_75181_e = new CraftingInventory(this, 2, 2);
   private final CraftResultInventory field_75179_f = new CraftResultInventory();
   public final boolean isLocalWorld;
   private final PlayerEntity player;

   public PlayerContainer(PlayerInventory playerInventory, boolean localWorld, PlayerEntity playerIn) {
      super((ContainerType<?>)null, 0);
      this.isLocalWorld = localWorld;
      this.player = playerIn;
      this.addSlot(new CraftingResultSlot(playerInventory.player, this.field_75181_e, this.field_75179_f, 0, 154, 28));

      for(int i = 0; i < 2; ++i) {
         for(int j = 0; j < 2; ++j) {
            this.addSlot(new Slot(this.field_75181_e, j + i * 2, 98 + j * 18, 18 + i * 18));
         }
      }

      for(int k = 0; k < 4; ++k) {
         final EquipmentSlotType equipmentslottype = VALID_EQUIPMENT_SLOTS[k];
         this.addSlot(new Slot(playerInventory, 39 - k, 8, 8 + k * 18) {
            public int getSlotStackLimit() {
               return 1;
            }

            public boolean isItemValid(ItemStack stack) {
               return stack.canEquip(equipmentslottype, player);
            }

            public boolean canTakeStack(PlayerEntity playerIn) {
               ItemStack itemstack = this.getStack();
               return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.canTakeStack(playerIn);
            }

            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> func_225517_c_() {
               return Pair.of(PlayerContainer.field_226615_c_, PlayerContainer.ARMOR_SLOT_TEXTURES[equipmentslottype.getIndex()]);
            }
         });
      }

      for(int l = 0; l < 3; ++l) {
         for(int j1 = 0; j1 < 9; ++j1) {
            this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
         }
      }

      for(int i1 = 0; i1 < 9; ++i1) {
         this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
      }

      this.addSlot(new Slot(playerInventory, 40, 77, 62) {
         @OnlyIn(Dist.CLIENT)
         public Pair<ResourceLocation, ResourceLocation> func_225517_c_() {
            return Pair.of(PlayerContainer.field_226615_c_, PlayerContainer.field_226620_h_);
         }
      });
   }

   public void func_201771_a(RecipeItemHelper p_201771_1_) {
      this.field_75181_e.fillStackedContents(p_201771_1_);
   }

   public void clear() {
      this.field_75179_f.clear();
      this.field_75181_e.clear();
   }

   public boolean matches(IRecipe<? super CraftingInventory> recipeIn) {
      return recipeIn.matches(this.field_75181_e, this.player.world);
   }

   public void onCraftMatrixChanged(IInventory inventoryIn) {
      WorkbenchContainer.func_217066_a(this.windowId, this.player.world, this.player, this.field_75181_e, this.field_75179_f);
   }

   public void onContainerClosed(PlayerEntity playerIn) {
      super.onContainerClosed(playerIn);
      this.field_75179_f.clear();
      if (!playerIn.world.isRemote) {
         this.clearContainer(playerIn, playerIn.world, this.field_75181_e);
      }
   }

   public boolean canInteractWith(PlayerEntity playerIn) {
      return true;
   }

   public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.inventorySlots.get(index);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         EquipmentSlotType equipmentslottype = MobEntity.getSlotForItemStack(itemstack);
         if (index == 0) {
            if (!this.mergeItemStack(itemstack1, 9, 45, true)) {
               return ItemStack.EMPTY;
            }

            slot.onSlotChange(itemstack1, itemstack);
         } else if (index >= 1 && index < 5) {
            if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (index >= 5 && index < 9) {
            if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (equipmentslottype.getSlotType() == EquipmentSlotType.Group.ARMOR && !this.inventorySlots.get(8 - equipmentslottype.getIndex()).getHasStack()) {
            int i = 8 - equipmentslottype.getIndex();
            if (!this.mergeItemStack(itemstack1, i, i + 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (equipmentslottype == EquipmentSlotType.OFFHAND && !this.inventorySlots.get(45).getHasStack()) {
            if (!this.mergeItemStack(itemstack1, 45, 46, false)) {
               return ItemStack.EMPTY;
            }
         } else if (index >= 9 && index < 36) {
            if (!this.mergeItemStack(itemstack1, 36, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (index >= 36 && index < 45) {
            if (!this.mergeItemStack(itemstack1, 9, 36, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.mergeItemStack(itemstack1, 9, 45, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
         } else {
            slot.onSlotChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);
         if (index == 0) {
            playerIn.dropItem(itemstack2, false);
         }
      }

      return itemstack;
   }

   public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
      return slotIn.inventory != this.field_75179_f && super.canMergeSlot(stack, slotIn);
   }

   public int getOutputSlot() {
      return 0;
   }

   public int getWidth() {
      return this.field_75181_e.getWidth();
   }

   public int getHeight() {
      return this.field_75181_e.getHeight();
   }

   @OnlyIn(Dist.CLIENT)
   public int getSize() {
      return 5;
   }
}