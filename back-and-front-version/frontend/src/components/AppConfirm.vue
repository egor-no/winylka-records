<template>
  <div
    v-if="visible"
    class="app-confirm-overlay"
    @click.self="cancel"
  >
    <div class="app-confirm">
      <div class="app-confirm-header">
        <span>{{ title }}</span>

        <button
          type="button"
          class="app-confirm-close"
          @click="cancel"
        >
          ×
        </button>
      </div>

      <div class="app-confirm-body">
        <p class="app-confirm-message">
          {{ message }}
        </p>

        <div class="app-confirm-actions">
          <button
            type="button"
            class="app-confirm-cancel"
            @click="cancel"
          >
            {{ cancelText }}
          </button>

          <button
            type="button"
            class="app-confirm-submit"
            :class="{ 'app-confirm-danger': danger }"
            @click="confirm"
          >
            {{ confirmText }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AppConfirm',

  props: {
    visible: {
      type: Boolean,
      required: true
    },

    title: {
      type: String,
      default: 'Winylka confirmation'
    },

    message: {
      type: String,
      default: ''
    },

    confirmText: {
      type: String,
      default: 'Confirm'
    },

    cancelText: {
      type: String,
      default: 'Cancel'
    },

    danger: {
      type: Boolean,
      default: false
    }
  },

  emits: ['confirm', 'cancel'],

  methods: {
    confirm() {
      this.$emit('confirm')
    },

    cancel() {
      this.$emit('cancel')
    }
  }
}
</script>

<style scoped>
.app-confirm-overlay {
  position: fixed;
  inset: 0;
  z-index: 1100;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(20, 47, 34, 0.55);
  backdrop-filter: blur(2px);
}

.app-confirm {
  width: min(460px, 100%);
  overflow: hidden;
  border: 1px solid #142f22;
  border-radius: 8px;
  background: #f6e4bf;
  box-shadow: 0 14px 40px rgba(20, 47, 34, 0.28);
}

.app-confirm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  background: #142f22;
  color: #e0c494;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.app-confirm-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  border: 1px solid #e0c494;
  border-radius: 4px;
  background: transparent;
  color: #e0c494;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
}

.app-confirm-close:hover {
  background: #e0c494;
  color: #142f22;
}

.app-confirm-body {
  padding: 16px;
}

.app-confirm-message {
  margin: 0;
  color: #3c2a1a;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-line;
}

.app-confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 18px;
}

.app-confirm-cancel,
.app-confirm-submit {
  padding: 7px 14px;
  border-radius: 5px;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  cursor: pointer;
}

.app-confirm-cancel {
  border: 1px solid #cfa66f;
  background: transparent;
  color: #142f22;
}

.app-confirm-cancel:hover {
  border-color: #142f22;
  background: #fff3d9;
}

.app-confirm-submit {
  border: 1px solid #142f22;
  background: #142f22;
  color: #e0c494;
}

.app-confirm-submit:hover {
  background: #1d402f;
}

.app-confirm-submit.app-confirm-danger {
  border-color: #6f1814;
  background: #a22a21;
  color: #fff8e7;
}

.app-confirm-submit.app-confirm-danger:hover {
  background: #7d201a;
}

@media (max-width: 520px) {
  .app-confirm-overlay {
    padding: 12px;
  }

  .app-confirm-actions {
    flex-direction: column-reverse;
  }

  .app-confirm-cancel,
  .app-confirm-submit {
    width: 100%;
  }
}
</style>