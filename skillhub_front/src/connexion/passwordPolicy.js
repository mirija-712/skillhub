const RULES = [
  { key: "length", label: "Au moins 12 caractères", test: (value) => value.length >= 12 },
  { key: "upper", label: "Une majuscule", test: (value) => /[A-Z]/.test(value) },
  { key: "lower", label: "Une minuscule", test: (value) => /[a-z]/.test(value) },
  { key: "digit", label: "Un chiffre", test: (value) => /\d/.test(value) },
  { key: "special", label: "Un caractère spécial", test: (value) => /[^A-Za-z0-9]/.test(value) },
];

export function evaluatePassword(password = "") {
  const checks = RULES.map((rule) => ({
    key: rule.key,
    label: rule.label,
    ok: rule.test(password),
  }));
  const score = checks.filter((item) => item.ok).length;

  let level = "empty";
  if (password.length > 0 && score <= 2) {
    level = "weak";
  } else if (score <= 4) {
    level = "medium";
  } else if (score === 5) {
    level = "strong";
  }

  return {
    checks,
    score,
    isCompliant: score === RULES.length,
    level,
  };
}
