<template>
  <section class="about-page">
    <h2 class="section-title">About Winylka Records</h2>

    <p class="about-text">
      Winylka Records — это воображаемый онлайн-магазин винила, где собрана
      коллекция пластинок. Здесь нет бесконечного каталога на тысячи позиций, 
      только те релизы, которые действительно хочется поставить на проигрыватель.
    </p>

    <p class="about-text">
      Winylka Records признан лучшим воображаемым музыкальным магазином 
      по версии воображаемых музыкальных наград в 2026-м году. 
    </p>

    <p class="about-img">
      <img class="award-img" src="/src/assets/best-award.png" />
    </p>

    <p class="about-text">
      Магазин создан как учебный и творческий проект: он помогает отработать
      работу с Vue, компонентами, фильтрами и корзиной, но при этом выглядит
      как настоящий минималистичный магазин винила.
    </p>

    <p class="about-text">
      В каталоге можно отфильтровать пластинки по цене, найти альбом по
      исполнителю или названию и добавить его в корзину. Интерфейс сделан
      максимально простым: ничего лишнего, только обложка, артист, альбом
      и цена.
    </p>
  </section>
</template>

<script>
export default {
  name: 'AboutPage'
}
</script>

<style scoped>
.about-page {
  max-width: 800px;
  margin: 0 auto;
}

.about-text {
  margin-top: 12px;
  font-size: 14px;
  line-height: 1.6;
  color: #444;
}

.about-img {
  margin-top: 12px;
  align: center;
}

.award-img {
  height: 80x;
  width: auto;
  border-radius: 4px;
}
</style>