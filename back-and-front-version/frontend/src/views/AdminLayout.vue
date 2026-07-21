<template>
  <section class="admin-layout">
    <nav class="admin-section-nav">
      <RouterLink
        to="/admin/products"
        class="admin-section-link"
      >
        Stock
      </RouterLink>

      <RouterLink
        to="/admin/orders"
        class="admin-section-link"
      >
        Orders
      </RouterLink>
    </nav>

    <RouterView v-slot="{ Component }">
      <component
        :is="Component"
        @notice="showNotice"
        @confirm="showConfirm"
      />
    </RouterView>

    <AppNotice
      :visible="notice.visible"
      :message="notice.message"
      :product="null"
      :subscription-type="null"
      @close="closeNotice"
    />

    <AppConfirm
      :visible="confirmation.visible"
      :title="confirmation.title"
      :message="confirmation.message"
      :confirm-text="confirmation.confirmText"
      :cancel-text="confirmation.cancelText"
      :danger="confirmation.danger"
      @confirm="confirmAction"
      @cancel="closeConfirmation"
    />
  </section>
</template>

<script>
import AppNotice from '../components/AppNotice.vue'
import AppConfirm from '../components/AppConfirm.vue'

export default {
  name: 'AdminLayout',

  components: {
    AppNotice,
    AppConfirm
  },

  data() {
    return {
      notice: {
        visible: false,
        message: ''
      },

      confirmation: {
        visible: false,
        title: 'Winylka confirmation',
        message: '',
        confirmText: 'Confirm',
        cancelText: 'Cancel',
        danger: false,
        action: null
      }
    }
  },

  methods: {
    showNotice(message) {
      this.notice = {
        visible: true,
        message
      }
    },

    closeNotice() {
      this.notice = {
        visible: false,
        message: ''
      }
    },

    showConfirm(options) {
      this.confirmation = {
        visible: true,
        title:
          options?.title ||
          'Winylka confirmation',
        message:
          options?.message || '',
        confirmText:
          options?.confirmText ||
          'Confirm',
        cancelText:
          options?.cancelText ||
          'Cancel',
        danger:
          Boolean(options?.danger),
        action:
          typeof options?.action === 'function'
            ? options.action
            : null
      }
    },

    confirmAction() {
      const action = this.confirmation.action

      this.closeConfirmation()

      if (action) {
        action()
      }
    },

    closeConfirmation() {
      this.confirmation = {
        visible: false,
        title: 'Winylka confirmation',
        message: '',
        confirmText: 'Confirm',
        cancelText: 'Cancel',
        danger: false,
        action: null
      }
    }
  }
}
</script>