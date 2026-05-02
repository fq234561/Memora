import {
  PromptOptimizeRequest,
  OptimizedPromptResult,
  ModelParams,
} from '../models/prompt';

// Relationship descriptors mapped to respectful memorial language
const relationshipSubjectMap: Record<string, string> = {
  parent:
    'a gentle, natural portrait of two people standing close together, one adult and one elder parent, showing familial warmth and quiet affection',
  spouse:
    'a tender, dignified portrait of two people who shared a lifelong partnership, standing or seated close with mutual warmth and devotion',
  sibling:
    'a warm, natural portrait of two people who grew up together, showing the easy familiarity and quiet love of siblings',
  grandparent:
    'a reverent, soft-focus portrait of an adult with their grandparent, capturing the gentle wisdom and unconditional love across generations',
  child:
    'a peaceful, intimate portrait of a parent with their child, expressing nurturing love and cherished memory',
  friend:
    'a sincere, natural portrait of two close friends, capturing the comfort and trust of a deep bond',
  other:
    'a respectful, natural portrait of two people with a meaningful connection, expressing quiet remembrance and affection',
};

// Photo type composition templates
const photoTypeCompositionMap: Record<string, string> = {
  natural_family:
    'medium shot, subjects centered or slightly off-center using rule of thirds, soft environmental background with gentle bokeh, natural candid posture rather than stiff posing',
  vintage_restore:
    'medium close-up, subjects framed with nostalgic warmth, sepia-tinged or faded color palette, subtle analog film grain, period-appropriate clothing and soft vignette edges',
  birthday:
    'medium shot, warm indoor or garden setting, soft ambient light from candles or string lights subtly visible in background, joyful but tender expressions, celebratory yet intimate',
  graduation_wedding_holiday:
    'medium to wide shot, ceremonial or festive backdrop with dignified composition, formal attire appropriate to the occasion, proud and peaceful posture',
};

// Style-specific color and texture direction
const styleDirectionMap: Record<string, string> = {
  NATURAL_FAMILY:
    'natural color grading with warm skin tones, soft matte finish, lifelike texture, minimal retouching',
  VINTAGE_RESTORE:
    'faded analog film aesthetic, warm sepia or desaturated tones, subtle grain and light leaks, time-worn paper texture',
  BIRTHDAY:
    'warm golden and cream palette, soft bokeh highlights, gentle contrast, cheerful but not oversaturated',
  GRADUATION_WEDDING_HOLIDAY:
    'rich but dignified color grading, elegant contrast, formal portrait lighting, timeless quality',
};

// Mood modifiers
const moodModifierMap: Record<string, string> = {
  warm: 'warm, comforting atmosphere with golden ambient light',
  nostalgic: 'soft, dreamlike nostalgia with gentle haze and muted highlights',
  peaceful: 'serene, tranquil mood with stillness and quiet dignity',
  celebratory: 'gentle celebration, quiet joy mixed with tender remembrance',
  solemn: 'dignified, reverent atmosphere with subdued tones and respectful stillness',
};

// Composition preference overrides
const compositionOverrideMap: Record<string, string> = {
  side_by_side: 'subjects standing shoulder to shoulder, facing slightly toward each other with relaxed posture',
  embrace: 'a gentle, respectful embrace or arm around shoulder, conveying love without dramatic gesture',
  seated_together: 'subjects seated closely on a sofa or bench, turned slightly toward one another in comfortable intimacy',
  holding_hands: 'subjects standing or walking with hands gently clasped, expressing quiet connection',
  looking_at_each_other: 'subjects facing one another with soft eye contact, expressions of fond recognition',
};

// Fixed memorial safety constraints injected into every prompt
const memorialSafetyConstraints = [
  'This is a private memorial image, not a historical photograph.',
  'AI-generated memorial portrait. Respectful and dignified tone only.',
  'Do not depict speech, movement, animation, or resurrection of the deceased.',
  'Do not include text artifacts, watermarks, signatures, or commercial branding.',
  'Preserve the facial identity and dignified appearance of the subjects from the provided reference photos.',
];

// Fixed negative prompt (exclusion list)
const fixedNegativePrompt =
  'sensual, revealing clothing, commercial advertisement, brand logos, text overlays, cartoonish, anime, manga, distorted facial features, extra limbs, unnatural skin texture, plastic skin, gore, horror, resurrection animation, speaking, moving, laughing exaggeratedly, religious symbolism, political imagery, crowded background, harsh flash photography, oversaturated colors, low quality, blurry, deformed hands';

// Model params recommendation
const defaultModelParams: ModelParams = {
  size: '1024x1536',
  quality: 'high',
  style: 'natural',
};

export class PromptOptimizer {
  build(request: PromptOptimizeRequest): OptimizedPromptResult {
    const {
      userDescription,
      relationship,
      photoType,
      style,
      mood,
      compositionPrefs,
    } = request;

    // 1. Subject
    const subjectBase =
      relationshipSubjectMap[relationship.toLowerCase()] ||
      relationshipSubjectMap['other'];
    const subject = userDescription
      ? `${subjectBase}. Additional context from the user: ${userDescription}`
      : subjectBase;

    // 2. Composition
    const compositionBase =
      photoTypeCompositionMap[photoType.toLowerCase()] ||
      photoTypeCompositionMap['natural_family'];
    const composition = compositionPrefs
      ? `${compositionOverrideMap[compositionPrefs.toLowerCase()] || compositionBase}. Overall framing: ${compositionBase}`
      : compositionBase;

    // 3. Style direction
    const styleDirection =
      styleDirectionMap[style] ||
      styleDirectionMap['NATURAL_FAMILY'];

    // 4. Mood
    const moodText = mood
      ? moodModifierMap[mood.toLowerCase()] || `a ${mood} atmosphere`
      : 'a warm, reverent atmosphere of quiet remembrance';

    // 5. Lighting & technicals
    const lightingTechnicals =
      'Soft diffused natural light or warm window light. ' +
      '85mm portrait lens look at f/2.0, shallow depth of field with creamy bokeh. ' +
      'Hyper-realistic skin texture, individual hair strands visible, natural pores and soft shadows. ' +
      'Slight film grain for emotional warmth. ' +
      'Vertical portrait orientation (3:4 aspect ratio), medium close-up to medium shot framing.';

    // 6. Safety constraints paragraph
    const safetyParagraph = memorialSafetyConstraints.join(' ');

    // Assemble optimized prompt
    const optimizedPrompt = [
      `Subject: ${subject}.`,
      `Composition: ${composition}.`,
      `Style & Color: ${styleDirection}.`,
      `Mood: ${moodText}.`,
      `Lighting & Technicals: ${lightingTechnicals}`,
      `Safety & Context: ${safetyParagraph}`,
    ].join('\n\n');

    // Style keywords (shorter tag-line version)
    const styleKeyword = style.toLowerCase().replace(/_/g, ' ');
    const photoTypeKeyword = photoType.toLowerCase().replace(/_/g, ' ');
    const stylePrompt = [
      'memorial portrait',
      'photorealistic',
      'respectful',
      'dignified',
      'natural lighting',
      'shallow depth of field',
      ...(styleKeyword !== photoTypeKeyword ? [styleKeyword, photoTypeKeyword] : [styleKeyword]),
      'private remembrance',
    ].join(', ');

    return {
      optimizedPrompt,
      negativePrompt: fixedNegativePrompt,
      stylePrompt,
      safetyNotes: [...memorialSafetyConstraints],
      modelParams: { ...defaultModelParams },
    };
  }
}

export const promptOptimizer = new PromptOptimizer();
