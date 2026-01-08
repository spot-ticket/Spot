import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Menu, MenuOption, CartItem, Cart } from '@/types';

interface CartState {
  cart: Cart | null;
  addItem: (storeId: string, storeName: string, menu: Menu, quantity: number, options: MenuOption[]) => void;
  removeItem: (menuId: string) => void;
  updateQuantity: (menuId: string, quantity: number) => void;
  clearCart: () => void;
  getTotal: () => number;
  getItemCount: () => number;
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      cart: null,

      addItem: (storeId, storeName, menu, quantity, options) => {
        const { cart } = get();

        // 다른 가게의 메뉴가 있으면 장바구니 비우기
        if (cart && cart.storeId !== storeId) {
          const confirm = window.confirm(
            '다른 가게의 메뉴가 장바구니에 있습니다. 장바구니를 비우고 새로 담으시겠습니까?'
          );
          if (!confirm) return;
          set({ cart: null });
        }

        const currentCart = get().cart;
        const newItem: CartItem = { menu, quantity, selectedOptions: options };

        if (!currentCart) {
          set({
            cart: {
              storeId,
              storeName,
              items: [newItem],
            },
          });
          return;
        }

        // 같은 메뉴가 있으면 수량 추가
        const existingIndex = currentCart.items.findIndex(
          (item) =>
            item.menu.id === menu.id &&
            JSON.stringify(item.selectedOptions) === JSON.stringify(options)
        );

        if (existingIndex >= 0) {
          const updatedItems = [...currentCart.items];
          updatedItems[existingIndex].quantity += quantity;
          set({ cart: { ...currentCart, items: updatedItems } });
        } else {
          set({
            cart: {
              ...currentCart,
              items: [...currentCart.items, newItem],
            },
          });
        }
      },

      removeItem: (menuId) => {
        const { cart } = get();
        if (!cart) return;

        const updatedItems = cart.items.filter((item) => item.menu.id !== menuId);
        if (updatedItems.length === 0) {
          set({ cart: null });
        } else {
          set({ cart: { ...cart, items: updatedItems } });
        }
      },

      updateQuantity: (menuId, quantity) => {
        const { cart } = get();
        if (!cart) return;

        if (quantity <= 0) {
          get().removeItem(menuId);
          return;
        }

        const updatedItems = cart.items.map((item) =>
          item.menu.id === menuId ? { ...item, quantity } : item
        );
        set({ cart: { ...cart, items: updatedItems } });
      },

      clearCart: () => set({ cart: null }),

      getTotal: () => {
        const { cart } = get();
        if (!cart) return 0;

        return cart.items.reduce((total, item) => {
          const optionsTotal = item.selectedOptions.reduce(
            (sum, opt) => sum + opt.optionPrice,
            0
          );
          return total + (item.menu.price + optionsTotal) * item.quantity;
        }, 0);
      },

      getItemCount: () => {
        const { cart } = get();
        if (!cart) return 0;
        return cart.items.reduce((count, item) => count + item.quantity, 0);
      },
    }),
    {
      name: 'cart-storage',
    }
  )
);

export default useCartStore;
