from PIL import Image, ImageDraw, ImageFont, ImageFilter

def create_feature_graphic(output_path="play_store_assets/feature_graphic_1024x500.png"):
    width, height = 1024, 500
    img = Image.new("RGBA", (width, height), (15, 20, 28, 255))
    draw = ImageDraw.Draw(img)

    # 1. Background gradient & subtle ambient glow
    # Dark radial glow on the left (behind tiles) and top right
    glow = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(glow)
    gdraw.ellipse([50, 50, 450, 450], fill=(46, 125, 50, 35))
    gdraw.ellipse([650, -50, 1050, 350], fill=(30, 58, 138, 30))
    gdraw.ellipse([800, 200, 1100, 500], fill=(245, 124, 0, 25))
    glow = glow.filter(ImageFilter.GaussianBlur(radius=70))
    img.paste(Image.alpha_composite(Image.new("RGBA", (width, height), (0, 0, 0, 0)), glow), (0, 0), glow)
    draw = ImageDraw.Draw(img)

    # 2. Draw Left Grid (6 rows x 5 columns)
    tile_size = 54
    tile_gap = 10
    start_x = 45
    start_y = 55
    rows = 6
    cols = 5

    # Fonts
    font_tile = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 32)
    font_title = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 58)
    font_sub = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 20)
    font_desc = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', 15)
    font_badge = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 13)

    # Draw background sunken wells for all rows
    for r in range(rows):
        for c in range(cols):
            x = start_x + c * (tile_size + tile_gap)
            y = start_y + r * (tile_size + tile_gap)
            draw.rounded_rectangle([x, y, x + tile_size, y + tile_size], radius=10, outline=(30, 41, 59, 140), width=2)

    # In Row 2 (index 2), draw active 3D extruded tiles: "K I S I A"
    word = "KISIA"
    tile_colors = [
        ((46, 125, 50), (27, 94, 32), (102, 187, 106)),   # Green (K)
        ((245, 124, 0), (230, 81, 0), (255, 183, 77)),   # Orange (I)
        ((46, 125, 50), (27, 94, 32), (102, 187, 106)),   # Green (S)
        ((51, 65, 85), (30, 41, 59), (100, 116, 139)),   # Slate (I)
        ((46, 125, 50), (27, 94, 32), (102, 187, 106)),   # Green (A)
    ]
    r = 2
    for c, char in enumerate(word):
        x = start_x + c * (tile_size + tile_gap) - 4
        y = start_y + r * (tile_size + tile_gap) - 4
        w_size = tile_size + 8
        face_col, lip_col, rim_col = tile_colors[c]

        # Shadow
        draw.rounded_rectangle([x, y + 8, x + w_size, y + w_size + 8], radius=14, fill=(0, 0, 0, 160))
        # Extruded lip
        draw.rounded_rectangle([x, y + 6, x + w_size, y + w_size + 6], radius=14, fill=lip_col)
        # Face
        draw.rounded_rectangle([x, y, x + w_size, y + w_size], radius=14, fill=face_col)
        # Rim highlight
        draw.rounded_rectangle([x + 2, y + 2, x + w_size - 2, y + 10], radius=6, fill=rim_col)
        # Letter
        draw.text((x + w_size // 2, y + w_size // 2), char, font=font_tile, fill=(255, 255, 255, 255), anchor="mm")

    # 3. Right Content Area
    right_x = 430
    curr_y = 65

    # Top pill badge: TACTILE 3D WORD DEDUCTION
    pill_w = 230
    draw.rounded_rectangle([right_x, curr_y, right_x + pill_w, curr_y + 30], radius=15, fill=(46, 125, 50, 60), outline=(74, 222, 128, 120), width=1)
    draw.text((right_x + pill_w // 2, curr_y + 15), "TACTILE 3D PUZZLE", font=font_badge, fill=(134, 239, 172, 255), anchor="mm")
    curr_y += 48

    # Title: NenoKisia
    draw.text((right_x, curr_y), "NenoKisia", font=font_title, fill=(255, 255, 255, 255))
    curr_y += 75

    # Subtitle: The Tactile Word Deduction Game
    draw.text((right_x, curr_y), "The Tactile Word Deduction Game", font=font_sub, fill=(241, 245, 249, 255))
    curr_y += 38

    # Feature bullet text
    draw.text((right_x, curr_y), "2.5D Extruded Tiles • Mechanical 3D Keycaps", font=font_desc, fill=(148, 163, 184, 255))
    curr_y += 24
    draw.text((right_x, curr_y), "Hardware Gyroscope Parallax • Multi-Board & RPG Campaign", font=font_desc, fill=(148, 163, 184, 255))
    curr_y += 50

    # Badges Row
    badges = [
        ("100% OFFLINE", (46, 125, 50, 80), (74, 222, 128, 160), (220, 252, 231)),
        ("FAIR PLAY", (30, 41, 59, 140), (100, 116, 139, 160), (241, 245, 249)),
        ("DUOS & QUADS", (245, 124, 0, 80), (251, 146, 60, 160), (254, 243, 199)),
        ("STREAK SHIELD", (2, 132, 199, 80), (56, 189, 248, 160), (224, 242, 254)),
    ]

    bx = right_x
    for text, fill_c, out_c, text_c in badges:
        bw = int(draw.textlength(text, font=font_badge)) + 24
        draw.rounded_rectangle([bx, curr_y, bx + bw, curr_y + 36], radius=18, fill=fill_c, outline=out_c, width=1)
        draw.text((bx + bw // 2, curr_y + 18), text, font=font_badge, fill=text_c, anchor="mm")
        bx += bw + 12

    img.save(output_path, "PNG", optimize=True)
    print(f"Feature graphic generated at {output_path} ({width}x{height})")

create_feature_graphic()
