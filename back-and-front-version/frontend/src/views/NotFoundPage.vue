<template>
    <div class="not-found-inner">
      <p class="not-found-kicker">Winylka Records</p>

      <h1 class="not-found-title">
        404: not found
      </h1>

      <div class="not-found-image-clip">
        <img :src="broken" alt="Broken record" class="not-found-image">
      </div>

      <p class="not-found-text">
        Looks like this record is out of print, mistyped, or never existed in our crates.
      </p>

      <RouterLink to="/catalogue" class="not-found-link">
        ← Back to catalogue
      </RouterLink>
    </div>
</template>

<script>
import broken from '../assets/broken-record.png'

export default {
  name: 'NotFoundPage',
  data() {
    return {
      broken
    }
  }
}
</script>

<style scoped>
.not-found-inner {
  max-width: 520px;
  width: 100%;
  margin: 0 auto;
  padding: 24px 26px 28px;

  background: #f6e4bf;
  border: 1px solid #cfa66f;
  border-radius: 8px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.25);

  text-align: center;
}

.not-found-kicker {
  margin: 0 0 6px;
  font-size: 11px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #6b5335;
}

.not-found-image-clip {
  width: 180px;     
  height: 100px;    
  margin: 0 auto 18px;
  overflow: hidden;
  border-radius: 6px;
}

.not-found-image {
  width: 100%;
  height: auto;
  display: block;

  object-position: center -30px;  
}

.not-found-title {
  margin: 0 0 10px;
  font-family: "MyGaramond", serif;
  font-size: 32px;
  line-height: 1.1;
  color: #142f22;
}

.not-found-text {
  margin: 0 0 18px;
  font-size: 14px;
  line-height: 1.5;
  color: #3c2a1a;
}

.not-found-link {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 999px;

  border: 1px solid #142f22;
  background: #142f22;
  color: #e0c494;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  text-decoration: none;

  transition:
    background 0.18s ease,
    color 0.18s ease,
    border-color 0.18s ease,
    transform 0.12s ease;
}

.not-found-link:hover {
  background: #1d402f;
  border-color: #c9a873;
  transform: translateY(-1px);
}
</style>