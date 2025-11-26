export function formatCurrency(value) {
  if (value == null || value === '') return '$0.00';

  const num = Number.parseFloat(value);
  if (Number.isNaN(num)) return '$0.00';

  return `$${num.toFixed(2)}`;
}