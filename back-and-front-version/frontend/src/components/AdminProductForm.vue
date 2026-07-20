<template>
  <div
    class="admin-modal-overlay"
    @click.self="requestClose"
  >
    <section class="admin-dialog product-editor">
      <div class="admin-dialog-titlebar">
        <span>
          {{ isEditMode ? 'EDIT PRODUCT' : 'NEW PRODUCT' }}
        </span>

        <button
          type="button"
          class="window-close-btn"
          :disabled="saving"
          @click="requestClose"
        >
          ×
        </button>
      </div>

      <form
        class="product-form"
        @submit.prevent="submitForm"
      >
        <div class="product-form-fields">
          <label class="form-field">
            <span>Artist *</span>

            <input
              v-model.trim="form.artist"
              type="text"
              maxlength="255"
              required
            >
          </label>

          <label class="form-field">
            <span>Album title *</span>

            <input
              v-model.trim="form.name"
              type="text"
              maxlength="255"
              required
            >
          </label>

          <div class="form-row">
            <label class="form-field">
              <span>Cat. No.</span>

              <input
                v-model.trim="form.catNo"
                type="text"
                maxlength="100"
              >
            </label>

            <label class="form-field">
              <span>Format</span>

              <input
                v-model.trim="form.format"
                type="text"
                maxlength="100"
                placeholder="LP, CD, 7 inch..."
              >
            </label>
          </div>

          <label class="form-field">
            <span>Note</span>

            <input
              v-model.trim="form.note"
              type="text"
              maxlength="255"
              placeholder="Limited edition, remastered..."
            >
          </label>

          <div class="form-row">
            <label class="form-field">
              <span>Price *</span>

              <input
                v-model="form.price"
                type="number"
                min="0"
                step="0.01"
                required
              >
            </label>

            <label class="form-field">
              <span>Initial stock *</span>

              <input
                v-model="form.stockQuantity"
                type="number"
                min="0"
                step="1"
                required
              >
            </label>
          </div>

          <label class="form-field">
            <span>Description</span>

            <textarea
              v-model.trim="form.description"
              rows="6"
              maxlength="3000"
            />
          </label>

          <label class="form-field">
            <span>
              {{ isEditMode ? 'Replace cover' : 'Cover image' }}
            </span>

            <input
              ref="imageInput"
              type="file"
              accept="image/jpeg,image/png,image/webp"
              @change="handleImageChange"
            >
          </label>

          <p
            v-if="isEditMode"
            class="form-note"
          >
            Leave the file field empty to keep the current cover.
          </p>

          <p
            v-if="error"
            class="form-error"
          >
            {{ error }}
          </p>
        </div>

        <div class="product-cover-panel">
          <div class="cover-panel-title">
            COVER PREVIEW
          </div>

          <div class="cover-preview-frame">
            <img
              v-if="previewUrl"
              :src="previewUrl"
              alt="Cover preview"
              class="cover-preview"
            >

            <div
              v-else
              class="cover-preview-empty"
            >
              NO IMAGE
            </div>
          </div>

          <p
            v-if="selectedFile"
            class="selected-file-name"
          >
            {{ selectedFile.name }}
          </p>
        </div>

        <div class="product-form-actions">
          <button
            type="submit"
            class="dialog-btn dialog-btn-primary"
            :disabled="saving"
          >
            {{ saving ? 'SAVING...' : 'SAVE' }}
          </button>

          <button
            type="button"
            class="dialog-btn"
            :disabled="saving"
            @click="requestClose"
          >
            CANCEL
          </button>
        </div>
      </form>
    </section>
  </div>
</template>

<script>
export default {
  name: 'AdminProductForm',

  props: {
    product: {
      type: Object,
      default: null
    },

    saving: {
      type: Boolean,
      default: false
    }
  },

  emits: ['save', 'close'],

  data() {
    return {
      form: {
        artist: '',
        name: '',
        catNo: '',
        format: '',
        note: '',
        price: '',
        description: '',
        stockQuantity: 0
      },

      selectedFile: null,
      previewUrl: '',
      generatedPreviewUrl: '',
      error: ''
    }
  },

  computed: {
    isEditMode() {
      return Boolean(this.product)
    },

    isDirty() {
      return (
        this.form.artist ||
        this.form.name ||
        this.form.catNo ||
        this.form.format ||
        this.form.note ||
        this.form.description ||
        this.form.price !== '' ||
        Number(this.form.stockQuantity) > 0 ||
        this.selectedFile
      )
    }
  },

  created() {
    this.fillForm()
  },

  beforeUnmount() {
    this.revokeGeneratedPreview()
  },

  methods: {
    fillForm() {
      if (!this.product) {
        return
      }

      this.form = {
        artist: this.product.artist || '',
        name: this.product.name || '',
        catNo: this.product.catNo || '',
        format: this.product.format || '',
        note: this.product.note || '',
        price: this.product.price ?? '',
        description: this.product.description || '',
        stockQuantity: this.product.stockQuantity ?? 0
      }

      this.previewUrl = this.product.img || ''
    },

    handleImageChange(event) {
      const file = event.target.files?.[0] || null

      this.revokeGeneratedPreview()
      this.selectedFile = null

      if (!file) {
        this.previewUrl = this.product?.img || ''
        return
      }

      const allowedTypes = [
        'image/jpeg',
        'image/png',
        'image/webp'
      ]

      if (!allowedTypes.includes(file.type)) {
        this.error = 'Only JPG, PNG and WEBP files are allowed.'
        event.target.value = ''
        this.previewUrl = this.product?.img || ''
        return
      }

      this.error = ''
      this.selectedFile = file
      this.generatedPreviewUrl = URL.createObjectURL(file)
      this.previewUrl = this.generatedPreviewUrl
    },

    revokeGeneratedPreview() {
      if (this.generatedPreviewUrl) {
        URL.revokeObjectURL(this.generatedPreviewUrl)
        this.generatedPreviewUrl = ''
      }
    },

    requestClose() {
      if (!this.isDirty) {
        this.$emit('close')
        return
      }

      if (window.confirm('Discard unsaved changes?')) {
        this.$emit('close')
      }
    },

    submitForm() {
      this.error = ''

      const price = Number(this.form.price)
      const stockQuantity = Number(this.form.stockQuantity)

      if (!this.form.artist || !this.form.name) {
        this.error = 'Artist and album title are required.'
        return
      }

      if (!Number.isFinite(price) || price < 0) {
        this.error = 'Price must be zero or greater.'
        return
      }

      if (
        !Number.isInteger(stockQuantity) ||
        stockQuantity < 0
      ) {
        this.error = 'Stock must be a non-negative whole number.'
        return
      }

      const formData = new FormData()

      formData.append('artist', this.form.artist)
      formData.append('name', this.form.name)
      formData.append('catNo', this.form.catNo || '')
      formData.append('format', this.form.format || '')
      formData.append('note', this.form.note || '')
      formData.append('price', price.toString())
      formData.append(
        'description',
        this.form.description || ''
      )
      formData.append(
        'stockQuantity',
        stockQuantity.toString()
      )

      if (this.selectedFile) {
        formData.append('image', this.selectedFile)
      }

      this.$emit('save', formData)
    }
  }
}
</script>

<style scoped>
.admin-modal-overlay {
  position: fixed;
  z-index: 1000;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(20, 18, 14, 0.66);
}

.admin-dialog {
  width: min(760px, 100%);
  max-height: calc(100vh - 40px);
  overflow: auto;
  background: #d8c59d;
  border-top: 3px solid #fff3d9;
  border-left: 3px solid #fff3d9;
  border-right: 3px solid #60492f;
  border-bottom: 3px solid #60492f;
  box-shadow: 7px 7px 0 rgba(0, 0, 0, 0.45);
}

.admin-dialog-titlebar {
  position: sticky;
  z-index: 2;
  top: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 28px;
  padding: 3px 5px 3px 8px;
  color: #e0c494;
  font-family: "Courier New", monospace;
  font-size: 13px;
  font-weight: bold;
  background: linear-gradient(
    90deg,
    #142f22,
    #28543d,
    #142f22
  );
}

.window-close-btn {
  width: 24px;
  height: 22px;
  padding: 0;
  color: #3c2a1a;
  font-size: 18px;
  line-height: 16px;
  cursor: pointer;
  background: #d8c59d;
  border-top: 2px solid #fff3d9;
  border-left: 2px solid #fff3d9;
  border-right: 2px solid #60492f;
  border-bottom: 2px solid #60492f;
}

.product-form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 210px;
  gap: 14px;
  padding: 14px;
}

.product-form-fields {
  min-width: 0;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.form-field {
  display: block;
  margin-bottom: 10px;
}

.form-field > span {
  display: block;
  margin-bottom: 3px;
  font-family: "Courier New", monospace;
  font-size: 11px;
  font-weight: bold;
  text-transform: uppercase;
}

.form-field input,
.form-field textarea {
  box-sizing: border-box;
  width: 100%;
  padding: 6px 7px;
  color: #3c2a1a;
  font-family: inherit;
  font-size: 13px;
  background: #fff8e7;
  border-top: 2px solid #60492f;
  border-left: 2px solid #60492f;
  border-right: 2px solid #fff3d9;
  border-bottom: 2px solid #fff3d9;
  border-radius: 0;
}

.form-field textarea {
  resize: vertical;
}

.form-note {
  margin: -4px 0 10px;
  color: #6f5435;
  font-size: 11px;
}

.form-error {
  padding: 7px;
  color: #fff8e7;
  font-family: "Courier New", monospace;
  font-size: 11px;
  background: #8f2e2e;
  border: 2px solid #551414;
}

.product-cover-panel {
  align-self: flex-start;
  padding: 8px;
  background: #cdb889;
  border-top: 2px solid #60492f;
  border-left: 2px solid #60492f;
  border-right: 2px solid #fff3d9;
  border-bottom: 2px solid #fff3d9;
}

.cover-panel-title {
  margin-bottom: 7px;
  font-family: "Courier New", monospace;
  font-size: 11px;
  font-weight: bold;
  text-align: center;
}

.cover-preview-frame {
  padding: 5px;
  background: #fff8e7;
  border: 1px solid #7a5a32;
}

.cover-preview {
  display: block;
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
}

.cover-preview-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  aspect-ratio: 1;
  color: #7a5a32;
  font-family: "Courier New", monospace;
  font-size: 11px;
  background:
    repeating-linear-gradient(
      135deg,
      #f6e4bf,
      #f6e4bf 7px,
      #ecd1a0 7px,
      #ecd1a0 14px
    );
}

.selected-file-name {
  overflow: hidden;
  margin: 7px 0 0;
  font-family: "Courier New", monospace;
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-form-actions {
  grid-column: 1 / -1;
  display: flex;
  justify-content: flex-end;
  gap: 7px;
  padding-top: 10px;
  border-top: 1px solid #8e744d;
}

.dialog-btn {
  min-width: 90px;
  padding: 6px 12px;
  font-family: inherit;
  font-size: 11px;
  font-weight: bold;
  cursor: pointer;
  background: #e0c494;
  border-top: 2px solid #fff3d9;
  border-left: 2px solid #fff3d9;
  border-right: 2px solid #60492f;
  border-bottom: 2px solid #60492f;
}

.dialog-btn:active {
  border-top-color: #60492f;
  border-left-color: #60492f;
  border-right-color: #fff3d9;
  border-bottom-color: #fff3d9;
}

.dialog-btn-primary {
  color: #e0c494;
  background: #142f22;
  border-top-color: #416b54;
  border-left-color: #416b54;
  border-right-color: #071b11;
  border-bottom-color: #071b11;
}

.dialog-btn:disabled,
.window-close-btn:disabled {
  cursor: default;
  opacity: 0.55;
}

@media (max-width: 700px) {
  .product-form {
    grid-template-columns: 1fr;
  }

  .product-cover-panel {
    width: 190px;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>